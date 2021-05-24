package micro;

import micro.If.If;
import micro.gateway.CallAsync;
import micro.gateway.CallSync;
import micro.primitives.*;
import micro.primitives.Lists.*;
import nano.ingredients.Name;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static micro.If.If.iff;
import static micro.Names.*;
import static micro.PropagationType.*;
import static micro.ReRun.runAndCheck;
import static micro.primitives.Add.add;
import static micro.primitives.Eq.eq;
import static micro.primitives.Gt.gt;
import static micro.primitives.Mul.mul;
import static micro.primitives.Primitive.nop;
import static micro.primitives.Print.print;
import static micro.primitives.Sub.subInt;
import static org.junit.Assert.assertEquals;

public class MicroTest {
    private static final Integer _6 = 6;
    private static final Integer _5 = 5;
    private static final Integer _4 = 4;
    private static final Integer _3 = 3;
    private static final Integer _2 = 2;
    private static final Integer _1 = 1;
    private static final Integer _0 = 0;

    private final Address address = new Address(new byte[0], 1, 1);

    @Test
    public void testLet() {
        Node env = new Node(address, false, false);
        /*

        func f(){
            let a = 1;
            let b = a * a;
            return a + b;
          }

        main{
         f()
        }

        */

        ResultCollector result = new ResultCollector();
        Action resultListener = new Action(i -> result.set(i.values()));

        F f = f(env, nop, Names.a).returnAs(Names.output).label("f");
        F mul = f(env, mul(), Names.left, Names.right).label("mul").returnAs(Names.b);
        F add = f(env, add(), Names.left, Names.right).label("add");

        f.addPropagation(ping, CONST(env, 2).returnAs(Names.a).label("const:2"));
        f.addPropagation(Names.a, Names.left, mul);
        f.addPropagation(Names.a, Names.right, mul);

        f.addPropagation(Names.a, Names.left, add);
        f.addPropagation(Names.b, Names.right, add);

        F main = f(env, resultListener, Names.output).label("main");
        main.addPropagation(ping, f);

        _Ex ex = env.createExecution(main);

        env.start();
        try {
            ex.receive(Value.of(ping, 0, env.getTop()));
            Concurrent.await(() -> !result.isEmpty());
            assertEquals(6, result.get().get(0).get());
        } finally {
            env.close();
        }
    }

/* testSimpleFunc
    a   b
    \   /
      +
      |
    add(a,b)


    add(a,b)
      |
    main

*/

    @Test
    public void testSimpleFunc_useAddDirectly() {

        ReRun.InitialRun rInit = runAndCheck(_3, n -> {
            CallSync<Integer> sync = createSimpleFunc_useAddDirectly(n);
            sync.param(Names.a, 1);
            sync.param(Names.b, 2);
            return sync;
        });

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> log = new ArrayList<>(rInit.events().subList(0, rInit.events().size() - 2));
        System.out.println("*** RECOVERY ***");
        for (int i = 0; i < 10; i++) {
            ReRun.reReReReRunAndCheck(rInit.exId(), this::createSimpleFunc_useAddDirectly, log, _3);
        }

    }

    private CallSync<Integer> createSimpleFunc_useAddDirectly(Env env) {
        F main = f(env, print(), Names.result).label("main");
        F add = f(env, add(), Names.left, Names.right).label("add");

        main.addPropagation(Names.a, Names.left, add);
        main.addPropagation(Names.b, Names.right, add);

        CallSync<Integer> sync = CallSync.of(Integer.class, main, env);

        return sync;
    }


    @Test
    public void testSimpleFunc_usingFCall() {
        ReRun.InitialRun rInit = runAndCheck(_3, n -> {
            CallSync<Integer> sync = createSimpleFuncByFCall(n);
            sync.param(Names.a, 1);
            sync.param(Names.b, 2);
            return sync;
        });

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> log = rInit.events().subList(0, rInit.events().size() - 2);

        ReRun.reReReReRunAndCheck(rInit.exId(), this::createSimpleFuncByFCall, log, _3);
    }

    private CallSync<Integer> createSimpleFuncByFCall(Env env) {
        F main = f(env, print(), Names.result).label("main");
        F add = f(env, add(), Names.left, Names.right).label("add");

        // FCall is redundant here, main could interact with add directly
        F callAdd = new FCall(env, add).returnAs(result);

        main.addPropagation(Names.a, Names.left, callAdd);
        main.addPropagation(Names.b, Names.right, callAdd);

        CallSync<Integer> sync = CallSync.of(Integer.class, main, env);

        return sync;
    }


    /*
        function add(left,right)=left+right
        function mul(left,right)=left*right
        function trisum (a,b,c) = add(add(a, b), c)
        function trimul (a,b,c) = mul(mul(a, b), c)
        function calc(a,b,c) = sum(trisum(1,2,3), trimul(1,2,3))

        a   b
         \ /
   (add2) +   c
           \ /
            + (add1)
            |
            trisum (a,b,c)


        a   b
         \ /
   (mul1) *   c
           \ /
            * (mul2)
            |
            trimul   (a,b,c)


trisum(a,b,c)   trimul(a,b,c)
           \   /
             + (add)
             |
            calc   (a,b,c)



    * */

    private F createTriOp(Env env, F binOp, String label) {
        F op1 = new FCall(env, binOp).label(label + "_1");
        F op2 = new FCall(env, binOp).label(label + "_2");

        F triOp = f(env, nop, Names.a, Names.b, Names.c).label(label + "_triop");
        triOp.addPropagation(Names.a, op1);
        triOp.addPropagation(Names.b, op1);
        triOp.addPropagation(Names.c, Names.right, op1);

        // TODO a simpler mechanism to remap param names than by propagation would be good, whats fcall here after all?
        op1.addPropagation(Names.a, Names.left, op2);
        op1.addPropagation(Names.b, Names.right, op2);

        op2.returnAs(Names.left);
        op1.returnAs(Names.result);

        return triOp;
    }

    private F createCalc(Env env) {
        F calc = f(env, nop, Names.result).label("calc");

        // single instances of add and mul, accessed via FCalls
        F add = f(env, add(), Names.left, Names.right).label("add").returnAs(Names.result);
        F mul = f(env, mul(), Names.left, Names.right).label("mul").returnAs(Names.result);
        F triSum = createTriOp(env, add, "triSum").returnAs(Names.left);
        F triMul = createTriOp(env, mul, "triMul").returnAs(Names.right);

        F callAdd = new FCall(env, add).returnAs(Names.result);
        calc.addPropagation(Names.a, callAdd);
        calc.addPropagation(Names.b, callAdd);
        calc.addPropagation(Names.c, callAdd);

        //TODO: shouldn't all propagations pass through the called function without being bypassed to triSum/triMul?
        callAdd.addPropagation(Names.a, triSum);
        callAdd.addPropagation(Names.b, triSum);
        callAdd.addPropagation(Names.c, triSum);

        callAdd.addPropagation(Names.a, triMul);
        callAdd.addPropagation(Names.b, triMul);
        callAdd.addPropagation(Names.c, triMul);
        return calc;
    }

    @Test
    public void testCalcSync() {
        Node env = new Node(address, false, false);
        F calc = createCalc(env);

        env.start();
        try {
            CallSync<Integer> sync = CallSync.of(Integer.class, calc, env);
            sync.param(Names.a, 1);
            sync.param(Names.c, 2);
            sync.param(Names.b, 3);

            assertEquals(Integer.valueOf(12), sync.call());
        } finally {
            env.close();
        }
    }

    @Test
    public void testCalcAsync() {
        Node env = new Node(address, false, false);
        F calc = createCalc(env);
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);

        env.start();
        try {

            CallAsync<Integer> call1 = CallAsync.of(Integer.class, calc, i1::addAndGet, env);
            CallAsync<Integer> call2 = CallAsync.of(Integer.class, calc, i2::addAndGet, env);

            call1.param(Names.a, 1);
            call1.param(Names.c, 2);
            call1.param(Names.b, 3);

            call2.param(Names.a, 2);
            call2.param(Names.c, 4);
            call2.param(Names.b, 3);

            call1.call();
            call2.call();
            Concurrent.await(() -> i1.get() > 0 && i2.get() > 0);
            assertEquals(Integer.valueOf(12), Integer.valueOf(i1.get()));
            assertEquals(Integer.valueOf(33), Integer.valueOf(i2.get()));
        } finally {
            env.close();
        }
    }

    /*

        def sub(a: Int, b: Int): Int = a - b

        def useFVar(a: Int, b: Int):Int = {
          val subtract  = sub
          subtract(a, b)
        }

       print(useFVar(2,1));

    */

    @Test
    public void testFunctionalValue() {
        ReRun.InitialRun rInit = runAndCheck(_1, n -> {
            CallSync<Integer> sync = createCallByFunctionalValue(n);
            sync.param(Names.a, 2);
            sync.param(Names.b, 1);
            return sync;
        });

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> log = rInit.events().subList(0, rInit.events().size() - 2);

        ReRun.reReReReRunAndCheck(rInit.exId(), this::createCallByFunctionalValue, log, _1);

    }

    private CallSync<Integer> createCallByFunctionalValue(Env env){
        F main = f(env, nop, Names.output).label("main");

        F sub = f(env, subInt(), Names.left, Names.right).label("sub");
        F useFVar = f(env, nop, Names.a, Names.b).label("useFVar").returnAs(result);

        // a plain functional value is a partially applied function with no partial parameters
        F createSubtract = new FCreatePartiallyAppliedFunction(env, sub).returnAs(paramFVar).label("createSubtract");

        F callSubtract = new FCallByFunctionalValue(env, paramFVar, Names.left, Names.right).label("callSubtract");

        useFVar.addPropagation(Names.a, ping, createSubtract);
        useFVar.addPropagation(paramFVar, callSubtract);
        useFVar.addPropagation(Name.a, Names.left, callSubtract);
        useFVar.addPropagation(Name.b, Names.right, callSubtract);

        main.addPropagation(Names.a, useFVar);
        main.addPropagation(Names.b, useFVar);

        return CallSync.of(Integer.class, main, env);
    }
    /*

        def sub(a: Int, b: Int): Int = a - b

        def usePartial(a: Int, b: Int):Int = {
          val dec : Int => Int = sub(_, b)
          dec(a)
        }

       print(usePartial(2,3));

    */

    @Test
    public void testPartialFunctionApplication() {
        Node env = new Node(address, false, false);
        ResultCollector result = new ResultCollector();
        Action resultListener = new Action(i -> result.set(i.values()));

        F main = f(env, resultListener, Names.output).label("main");

        F sub = f(env, subInt(), Names.left, Names.right).label("sub");
        F usePartial = f(env, nop, Names.a, Names.b).returnAs(Names.output).label("usePartial");

        F createDec = new FCreatePartiallyAppliedFunction(env, sub, Names.right).returnAs(paramFVar).label("createDec");
        F callDec = new FCallByFunctionalValue(env, paramFVar, Names.left).label("callDec");

        usePartial.addPropagation(Names.b, Names.right, createDec);
        usePartial.addPropagation(paramFVar, callDec);
        usePartial.addPropagation(Name.a, Names.left, callDec);

        main.addPropagation(Names.a, usePartial);
        main.addPropagation(Names.b, usePartial);

        _Ex ex = env.createExecution(main);

        env.start();

        ex.receive(Value.of(Names.a, 2, env.getTop()));
        ex.receive(Value.of(Names.b, 1, env.getTop()));

        Concurrent.await(() -> !result.isEmpty());
        assertEquals(1, result.get().get(0).get());

        env.close();
    }

    @Test
    public void testConst() {
        Node env = new Node(address, false, false);

        F main = f(env, print(), Names.result).label("main");
        F dec = f(env, nop, Names.a).label("dec");
        F sub = f(env, subInt(), Names.left, Names.right).label("sub");
        F one = f(env, new Constant(1)).returnAs(Names.right).label("const:one");

        main.addPropagation(ping, dec);
        dec.addPropagation(ping, sub);
        sub.addPropagation(ping, one);

        main.addPropagation(Names.a, dec);
        dec.addPropagation(Names.a, Names.left, sub);
        _Ex TOP = env.getTop();
        _Ex ex = env.createExecution(main);
        ex.receive(Value.of(ping, 0, TOP));
        ex.receive(Value.of(Names.a, 1, TOP));
    }

    @Test
    public void testIf() {
        ReRun.InitialRun rInit = runAndCheck(_2, this::createMax);

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> log = rInit.events().subList(0, rInit.events().size() - 2);

        ReRun.reReReReRunAndCheck(rInit.exId(), this::createMax, log, _2);
    }

    /*
     * max(left, right) = if(left > right) left else right
     *
     * */
    private CallSync<Integer> createMax(Env env) {
        F max = f(env, nop, Names.left, Names.right).label("max");
        If iff = iff(env).label("if");
        F gt = f(env, gt(), Names.left, Names.right).returnAs(Names.condition).label("gt");

        max.addPropagation(Names.left, iff);
        max.addPropagation(Names.right, iff);

        //TODO possible that nothing is stashed? Also in useFVar?
        iff.addPropagation(CONDITION, Names.left, gt);
        iff.addPropagation(CONDITION, Names.right, gt);
        iff.addPropagation(TRUE_BRANCH, Names.left, Names.result, iff);
        iff.addPropagation(FALSE_BRANCH, Names.right, Names.result, iff);

        CallSync<Integer> sync = CallSync.of(Integer.class, max, env);
        sync.param(Names.left, 2);
        sync.param(right, 1);
        return sync;
    }

    /* function geo(a) = if (a = 0)
                           0
                         else
                         {
                           let next_a = a - 1
                           a + geo(next_a);
                         }

       print(geo(3));
    */

    @Test
    public void testSimpleRecursion() {
        Node env = new Node(address, false, false);
        F main = createRecSum(env);
        //node.setDelay(1);
        env.start();

        _Ex m1 = env.createExecution(main);
        _Ex m2 = env.createExecution(main);

        m1.receive(Value.of(Names.a, 3, m1.returnTo()));
        m2.receive(Value.of(Names.a, 50, m1.returnTo()));

        Concurrent.sleep(8000);
        env.close();
    }

    @Test
    public void testResumeSimpleRecursion() {
        //resumeComputation(this::createRecSum); //TODO
    }

    private F createRecSum(Env env) {
        F main = f(env, print(), Names.result).label("main");
        F geo = f(env, nop, Names.a).label("geo");
        If iff = iff(env).label("if");

        main.addPropagation(Names.a, geo);
        geo.addPropagation(Names.a, iff);

        // condition
        F eq = f(env, eq(), Names.left, Names.right).returnAs(Names.condition).label("eq");

        iff.addPropagation(CONDITION, Names.a, Names.left, eq);
        eq.addPropagation(Names.left, ping, CONST(env, 0).returnAs(Names.right).label("zero:eq"));
        // onTrue
        iff.addPropagation(TRUE_BRANCH, Names.a, ping, CONST(env, 0).label("zero:ontrue"));
        // onFalse
        F block_else = f(env, nop).label("block_else");
        iff.addPropagation(FALSE_BRANCH, Names.a, block_else);
        // let next_a = a - 1
        String next_a = "next_a";
        F sub = f(env, subInt(), Names.left, Names.right).returnAs(next_a).label("sub");
        block_else.addPropagation(Names.a, Names.left, sub);
        sub.addPropagation(Names.left, ping, CONST(env, 1).returnAs(Names.right).label("one"));
        // a + geo(next_a);
        F add = f(env, add(), Names.left, Names.right).label("add");
        F geoReCall = new FCall(env, geo).returnAs(Names.right).label("geoCallR");

        block_else.addPropagation(Names.a, Names.left, add);
        block_else.addPropagation(next_a, add);
        add.addPropagation(next_a, Names.a, geoReCall);
        return main;
    }

    @Test
    public void testQuicksort() {
        Node env = new Node(address, false, false);

        ResultCollector result = new ResultCollector();
        F main = createQuicksortCall(env, result);

        env.start();
        try {
            testFor(env, result, main, list(), list());
            testFor(env, result, main, list(1), list(1));
            testFor(env, result, main, list(1, 2), list(1, 2));
            testFor(env, result, main, list(2, 1), list(1, 2));
            testFor(env, result, main, list(9, 0, 8, 1, 7, 2, 6, 3, 5, 4), list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

            ArrayList<Integer> randList = randomList(100);
            ArrayList<Integer> randListSorted = new ArrayList<>(randList);
            randListSorted.sort(Integer::compareTo);
            testFor(env, result, main, randList, randListSorted);
            System.out.println("Max exid used: " + (env.getNextExId() - 1));
        } finally {
            env.close();
        }
    }

    @Test
    public void testQuickReSort() {
        ArrayList<Integer> initial = randomList(100);//list(9, 0, 8, 1, 7, 2, 6, 3, 5, 4);
        ArrayList<Integer> randListSorted = new ArrayList<>(initial);
        randListSorted.sort(Integer::compareTo);
        ArrayList<Integer> expected = randListSorted;//list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        ReRun.InitialRun rInit = runAndCheck(expected, (n) -> createSyncQuicksort(n).param("list", initial));

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> log = rInit.events().subList(0, rInit.events().size() - 2);

        ReRun.reReReReRunAndCheck(rInit.exId(), (n) -> createSyncQuicksort(n).param("list", initial), log, expected);
    }

    @Test
    public void testQuicksortRerun() {
        ArrayList<Integer> initial = list(9, 0, 8, 1, 7, 2, 6, 3, 5, 4);
        ArrayList<Integer> expected = list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        Function<Env, CallSync<?>> createF = (n) -> createSyncQuicksort(n).param("list", initial);
        ReRun.InitialRun rInit = runAndCheck(expected, createF);

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> events = rInit.events().subList(0, rInit.events().size() - 2);
        for (int i = 0; i < events.size() - 2; i++) {
            ReRun.reRunAndCheck(rInit.exId(), createF, events.subList(0, events.size() - i), expected);
            System.out.print(i + " ");
        }

    }

    private CallSync<Object> createSyncQuicksort(Env env) {
        F quicksort = createQuicksort(env);
        F callQuicksort = new FCall(env, quicksort).returnAs(Names.output).label("callQuicksort initially");
        return CallSync.of(Object.class, callQuicksort, env);
    }

    private F createQuicksortCall(Env env, ResultCollector result) {
        Action resultListener = new Action(i -> result.set(i.values()));

        F main = f(env, resultListener, Names.output).label("main");
        F quicksort = createQuicksort(env);
        F callQuicksort = new FCall(env, quicksort).returnAs(Names.output).label("callQuicksort initially");
        main.addPropagation(list, callQuicksort);
        return main;
    }

        /*
        lteq: (u,v) -> boolean
        lteq: (u,v) -> boolean

        function quicksort(list) = {
            iff (list == []){
                []
            } else {
                let head = head(list);
                let tail = tail(list);
                let testGt = lteq(_, head)
                let testLteq = lteq(_, head)
                let filteredGt = filter(tail, testGt)
                let filteredLteq = filter(tail, testLteq)
                let sortedGt = quicksort(filteredGt)
                let sortedLteq = quicksort(filteredLteq)
                sortedLteq ::  head :: sortedGt
            }
        }
        */

    private F createQuicksort(Env env) {
        String predicateTestGt = "predicateTestGt";
        String predicateTestLteq = "predicateTestLteq";
        String filteredGt = "filteredGt";
        String filteredLteq = "filteredLteq";
        String sortedGt = "sortedGt";
        String sortedLteq = "sortedLteq";
        String consed = "consed";

        F quicksort = new F(env, nop, list).label("QUICKSORT(list)");
        If if_listEmpty = iff(env).label("if:listEmpty");
        quicksort.addPropagation(list, if_listEmpty);

        F isEmpty = new F(env, new IsEmpty(), list).returnAs(condition).label("isEmpty");
        if_listEmpty.addPropagation(CONDITION, list, isEmpty);
        F constEmptyList = new F(env, new Constant(Collections.emptyList()), ping).label("const:emptylist()");
        if_listEmpty.addPropagation(TRUE_BRANCH, list, ping, constEmptyList);

        F block_else = f(env, nop).label("block_else");
        if_listEmpty.addPropagation(FALSE_BRANCH, list, block_else);

        F head = new F(env, new Head(), list).returnAs(Names.head).label("head()");
        F tail = new F(env, new Tail(), list).returnAs(Names.tail).label("tail()");
        block_else.addPropagation(list, head);
        block_else.addPropagation(list, tail);

        F gt = f(env, Gt.gt(), Names.left, Names.right).label("gt?");
        F createTestGt = new FCreatePartiallyAppliedFunction(env, gt, Names.right).returnAs(predicateTestGt).label("createTestGt");
        block_else.addPropagation(Names.head, right, createTestGt);

        F lteq = f(env, Lteq.lteq(), Names.left, Names.right).label("lteq?");
        F createTestLteq = new FCreatePartiallyAppliedFunction(env, lteq, Names.right).returnAs(predicateTestLteq).label("createTestLteq");
        block_else.addPropagation(Names.head, right, createTestLteq);

        F filter = createFilter(env);
        F filterCallGt = new FCall(env, filter).returnAs(filteredGt).label("filterCallGt");
        F filterCallLteq = new FCall(env, filter).returnAs(filteredLteq).label("filterCallLteq");

        block_else.addPropagation(predicateTestGt, predicate, filterCallGt);
        block_else.addPropagation(predicateTestLteq, predicate, filterCallLteq);
        block_else.addPropagation(Names.tail, list, filterCallGt);
        block_else.addPropagation(Names.tail, list, filterCallLteq);

        F qsortRecallGt = new FCall(env, quicksort).returnAs(sortedGt).label("qsortRecallGt");
        F qsortRecallLteq = new FCall(env, quicksort).returnAs(sortedLteq).label("qsortRecallLteq");
        block_else.addPropagation(filteredGt, Names.list, qsortRecallGt);
        block_else.addPropagation(filteredLteq, Names.list, qsortRecallLteq);

        F cons = new F(env, new Cons(), element, list).returnAs(consed).label("cons");
        block_else.addPropagation(Names.head, Names.element, cons);
        block_else.addPropagation(sortedGt, list, cons);

        F concat = new F(env, new Concat(), left, right).label("concat");
        block_else.addPropagation(consed, right, concat);
        block_else.addPropagation(sortedLteq, Names.left, concat);


        return quicksort;
    }

    @Test
    public void testFilter() {
        Node env = new Node(address, false, false);
        ResultCollector result = new ResultCollector();
        Action resultListener = new Action(i -> result.set(i.values()));

        F main = createFilterTest(env, resultListener);

        env.start();
        testFor(env, result, main, list(), list());
        testFor(env, result, main, list(1), list());
        testFor(env, result, main, list(1, 2, 3), list());
        testFor(env, result, main, list(1, 2, 3, 4, 5), list(4, 5));
        testFor(env, result, main, list(5), list(5));
        env.close();
    }

    private F createFilterTest(Env env, Action resultListener) {
        F main = f(env, resultListener, Names.output).label("main");

        String const3 = "const:3";
        F gt = f(env, Gt.gt(), Names.left, Names.right).label("gt?");
        F createPredicate = new FCreatePartiallyAppliedFunction(env, gt, Names.right).returnAs(predicate).label("createPredicate");

        main.addPropagation(list, ping, createPredicate);
        createPredicate.addPropagation(ping, CONST(env, 3).returnAs(Names.right).label(const3));

        F filter = createFilter(env);
        F callFilter = new FCall(env, filter).returnAs(Names.output).label("callFilter initially");

        main.addPropagation(predicate, callFilter);
        main.addPropagation(list, callFilter);
        return main;
    }

    @Test
    public void testSuspendFilterTest() {
        Node env = new Node(address, false, false);
        ResultCollector result = new ResultCollector();
        Action resultListener = new Action(i -> result.set(i.values()));

        F main = createFilterTest(env, resultListener);

        env.start();
        env.setDelay(5);

        ArrayList<Integer> source = list(1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        _Ex ex = env.createExecution(main);
        ex.receive(Value.of(Names.list, source, env.getTop()));

        Concurrent.sleep(3000);
        env.stop();
        Concurrent.sleep(1000);
        env.close();
    }

    @Test
    public void testResumeFilterTest() {
        Node env = new Node(address, false, false);
        ResultCollector result = new ResultCollector();

        resumeComputation(() -> {
            Action resultListener = new Action(i -> result.set(i.values()));
            return createFilterTest(env, resultListener);
        });
    }

    private void testFor(Env env, ResultCollector resultCollector, F main, ArrayList<Integer> source, ArrayList<Integer> expected) {
        resultCollector.clear();
        _Ex ex = env.createExecution(main);
        ex.receive(Value.of(Names.list, source, env.getTop()));
        Concurrent.await(() -> !resultCollector.isEmpty());
        assertEquals(expected, resultCollector.get().get(0).get());
    }

    /*
        predicate: arg -> boolean

        function filter(list, predicate) = {
            iff (list == []){
                []
            } else {
                let head = head(list);
                let tail = tail(list);
                let tail_filtered = filter(tail, predicate)
                iff (predicate(head))
                    head :: tail_filtered
                else
                    tail_filtered
            }
        }
        */

    private F createFilter(Env env) {
        String tailFiltered = "tailFiltered";

        F filter = new F(env, nop, predicate, list);
        If if_listEmpty = iff(env).label("**if:listEmpty");
        filter.addPropagation(list, if_listEmpty);
        filter.addPropagation(predicate, if_listEmpty);

        F isEmpty = new F(env, new IsEmpty(), list).returnAs(condition).label("**isEmpty");
        if_listEmpty.addPropagation(CONDITION, list, isEmpty);
        F constEmptyList = new F(env, new Constant(Collections.emptyList()), ping).label("**const:emptylist()");
        if_listEmpty.addPropagation(TRUE_BRANCH, list, ping, constEmptyList);

        F block_else = f(env, nop).label("**block_else");
        if_listEmpty.addPropagation(FALSE_BRANCH, list, block_else);
        if_listEmpty.addPropagation(FALSE_BRANCH, predicate, block_else);

        F head = new F(env, new Head(), list).returnAs(Names.head).label("**head()");
        F tail = new F(env, new Tail(), list).returnAs(Names.tail).label("**tail()");
        F filterReCall = new FCall(env, filter).returnAs(tailFiltered).label("**filterReCall");

        If if_predicate = iff(env).label("**if:predicate");
        block_else.addPropagation(list, head);
        block_else.addPropagation(list, tail);
        block_else.addPropagation(Names.tail, Names.list, filterReCall);
        block_else.addPropagation(predicate, filterReCall);

        block_else.addPropagation(predicate, if_predicate);
        block_else.addPropagation(Names.head, if_predicate);
        block_else.addPropagation(tailFiltered, if_predicate);

        // TODO: underlying partial function takes left/right, right is already applied, left needs to be supplied
        // would be nice to have a remapping somewhere for the remaining 1 parameter, "left" -> "argument" or so
        F callPredicate = new FCallByFunctionalValue(env, predicate, Names.left).returnAs(condition).label("**callPredicateByFVal");

        if_predicate.addPropagation(CONDITION, predicate, callPredicate);
        if_predicate.addPropagation(CONDITION, Names.head, Names.left, callPredicate);

        F cons = new F(env, new Cons(), element, list).label("**cons");
        if_predicate.addPropagation(TRUE_BRANCH, Names.head, element, cons);
        if_predicate.addPropagation(TRUE_BRANCH, tailFiltered, Names.list, cons);

        if_predicate.addPropagation(FALSE_BRANCH, tailFiltered, new F(env, new Val(), tailFiltered).label("**VAL:tailFiltered"));

        return filter;
    }

    private F CONST(Env env, Object i) {
        return F.f(env, new Constant(i), ping);
    }

    private F f(Env env, Primitive primitive, String... params) {
        return F.f(env, primitive, params);
    }

    private ArrayList<Integer> list(Integer... i) {
        return new ArrayList<>(asList(i));
    }

    private ArrayList<Integer> randomList(int size) {
        ArrayList<Integer> list = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < size; i++) {
            list.add(rand.nextInt(size * 10));
        }

        return list;
    }

    private void resumeComputation(Supplier<_F> getF) {
        Node env = new Node(address, false, false);
        getF.get();
        env.setDelay(1);
        env.start(true);
        Concurrent.sleep(50000);
        env.close();
    }
}
