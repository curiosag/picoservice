package micro;

import micro.If.If;
import micro.event.ValueProcessedEvent;
import micro.event.eventlog.memeventlog.SimpleListEventLog;
import micro.gateway.Gateway;
import micro.primitives.*;
import micro.visualize.FVisualizer;
import nano.ingredients.Name;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static micro.Algorithm.*;
import static micro.If.If.iff;
import static micro.Names.*;
import static micro.PropagationType.*;
import static micro.ReRun.runAndCheck;
import static micro.primitives.Add.add;
import static micro.primitives.Mul.mul;
import static micro.primitives.Primitive.nop;
import static micro.primitives.Print.print;
import static org.junit.Assert.assertEquals;

public class MicroTest {
    private static final Integer _6 = 6;
    private static final Integer _5 = 5;
    private static final Integer _4 = 4;
    private static final Integer _3 = 3;
    private static final Integer _2 = 2;
    private static final Integer _1 = 1;
    private static final Integer _0 = 0;


    @Test
    public void testLet() {
        try (Node env = createNode()) {
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
            ex.receive(Value.of(ping, 0, env.getTop()));
            Concurrent.await(() -> !result.isEmpty());
            assertEquals(6, result.get().get(0).get());
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
            Gateway<Integer> sync = getSynchronized(n, createSimpleFunc_useAddDirectly(n));
            sync.param(Names.a, 1);
            sync.param(Names.b, 2);
            return sync;
        });

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> log = new ArrayList<>(rInit.events().subList(0, rInit.events().size() - 2));
        System.out.println("*** RECOVERY ***");
        for (int i = 0; i < 5; i++) {
            ReRun.reReReReRunAndCheck(rInit.exId(), (id, env) -> getSynchronized(id, env, createSimpleFunc_useAddDirectly(env)), log, _3);
        }

    }

    private Gateway<Integer> getSynchronized(Env env, F f) {
        return getSynchronized(Integer.class, env, f);
    }

    private Gateway<Integer> getSynchronized(Long relatchToId, Env env, F f) {
        return getSynchronized(Integer.class, relatchToId, env, f);
    }

    private <T> Gateway<T> getSynchronized(Class<T> resultType, Env env, F f) {
        return Gateway.of(resultType, f, env);
    }

    private <T> Gateway<T> getSynchronized(Class<T> resultType, Long relatchToId, Env env, F f) {
        return Gateway.of(relatchToId, resultType, f, env);
    }

    private F createSimpleFunc_useAddDirectly(Env env) {
        F main = f(env, print, Names.result).label("main");
        F add = f(env, add(), Names.left, Names.right).label("add");

        main.addPropagation(Names.a, Names.left, add);
        main.addPropagation(Names.b, Names.right, add);

        return main;
    }


    @Test
    public void testSimpleFunc_usingFCall() {
        ReRun.InitialRun rInit = runAndCheck(_3, n -> {
            Gateway<Integer> sync = getSynchronized(n, createSimpleFuncByFCall(n));
            sync.param(Names.a, 1);
            sync.param(Names.b, 2);
            return sync;
        });

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> log = rInit.events().subList(0, rInit.events().size() - 2);

        ReRun.reReReReRunAndCheck(rInit.exId(), (id, n) -> getSynchronized(id, n, createSimpleFuncByFCall(n)), log, _3);
    }

    private F createSimpleFuncByFCall(Env env) {
        F main = f(env, print, Names.result).label("main");
        F add = f(env, add(), Names.left, Names.right).label("add");

        // FCall is redundant here, main could interact with add directly
        F callAdd = new FCall(env, add).returnAs(result);

        main.addPropagation(Names.a, Names.left, callAdd);
        main.addPropagation(Names.b, Names.right, callAdd);

        return main;
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
        try (Node env = createNode()) {
            F calc = createCalc(env);
            env.start();
            Gateway<Integer> sync = Gateway.of(Integer.class, calc, env);
            sync.param(Names.a, 1);
            sync.param(Names.c, 2);
            sync.param(Names.b, 3);

            assertEquals(Integer.valueOf(12), sync.call());
        }
    }

    @Test
    public void testCalcAsyncConcurrently() {
        try (Node env = createNode()) {
            F calc = createCalc(env);
            final AtomicInteger i1 = new AtomicInteger(0);
            final AtomicInteger i2 = new AtomicInteger(0);
            env.start();
            Gateway<Integer> call1 = Gateway.of(Integer.class, calc, env);
            Gateway<Integer> call2 = Gateway.of(Integer.class, calc, env);

            call1.param(Names.a, 1);
            call1.param(Names.c, 2);
            call1.param(Names.b, 3);

            call2.param(Names.a, 2);
            call2.param(Names.c, 4);
            call2.param(Names.b, 3);

            call1.callAsync(i1::addAndGet);
            call2.callAsync(i2::addAndGet);
            Concurrent.await(() -> i1.get() > 0 && i2.get() > 0);
            assertEquals(Integer.valueOf(12), Integer.valueOf(i1.get()));
            assertEquals(Integer.valueOf(33), Integer.valueOf(i2.get()));
        }
    }

    /*

        def sub(a: Int, b: Int): Int = a - b

        def useFVar(a: Int, b: Int):Int = {
          val subtract  = sub
          subtract(a, b)
        }

    }

       print(useFVar(2,1));

    */

    @Test
    public void testFunctionalValue() {
        ReRun.InitialRun rInit = runAndCheck(_1, n -> {
            Gateway<Integer> sync = getSynchronized(n, createCallByFunctionalValue(n));
            sync.param(Names.a, 2);
            sync.param(Names.b, 1);
            return sync;
        });

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> log = rInit.events().subList(0, rInit.events().size() - 2);

        ReRun.reReReReRunAndCheck(rInit.exId(), (id, n) -> {
            Gateway<Integer> sync = getSynchronized(id, n, createCallByFunctionalValue(n));
            sync.param(Names.a, 2);
            sync.param(Names.b, 1);
            return sync;
        }, log, _1);
    }

    private F createCallByFunctionalValue(Env env) {
        F main = f(env, nop, Names.output).label("main");

        F sub = f(env, Sub.sub, Names.left, Names.right).label("sub");
        F useFVar = f(env, nop, Names.a, Names.b).label("useFVar").returnAs(result);

        // a plain functional value is a partially applied function with no partial parameters
        F createSubtract = new FCreatePartiallyAppliedFunction(env, sub).returnAs(paramFVar).label("createSubtract");

        F callSubtract = new FunctionalValueDefinition(env, paramFVar, Names.left, Names.right).label("callSubtract");

        useFVar.addPropagation(Names.a, ping, createSubtract);
        useFVar.addPropagation(paramFVar, callSubtract);
        useFVar.addPropagation(Name.a, Names.left, callSubtract);
        useFVar.addPropagation(Name.b, Names.right, callSubtract);

        main.addPropagation(Names.a, useFVar);
        main.addPropagation(Names.b, useFVar);

        return main;
    }

    @Test
    public void reTestPartialFunctionApplication() {

        ReRun.InitialRun rInit = runAndCheck(_4, n -> {
            F pfapp = createPFApp(n);
            Gateway<Integer> sync = getSynchronized(n, pfapp);
            sync.param(Names.a, 7);
            sync.param(Names.b, 3);
            return sync;
        });

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> log = rInit.events().subList(0, rInit.events().size() - 2);

        ReRun.reReReReRunAndCheck(rInit.exId(), (id, n) -> {
            F pfapp = createPFApp(n);
            Gateway<Integer> sync = getSynchronized(id, n, pfapp);
            sync.param(Names.a, 7);
            sync.param(Names.b, 3);
            return sync;
        }, log, _4);
    }

    /*

        def sub(a: Int, b: Int): Int = a - b

        def usePartial(a: Int, b: Int):Int = {
          val dec : Int => Int = sub(_, b)
          dec(a)
        }

       print(usePartial(2,3));

    */

    private F createPFApp(Env env){
        F main = f(env, nop, Names.output).label("main");

        F sub = f(env, Sub.sub, Names.left, Names.right).label("sub");
        F usePartial = f(env, nop, Names.a, Names.b).label("usePartial");

        F createDec = new FCreatePartiallyAppliedFunction(env, sub, Names.right).returnAs(paramFVar).label("createDec");
        F callDec = new FunctionalValueDefinition(env, paramFVar, Names.left).label("callDec");

        usePartial.addPropagation(Names.b, Names.right, createDec);
        usePartial.addPropagation(paramFVar, callDec);
        usePartial.addPropagation(Name.a, Names.left, callDec);

        main.addPropagation(Names.a, usePartial);
        main.addPropagation(Names.b, usePartial);
        return main;
    }


    @Test
    public void testConst() {
        try (Node env = createNode()) {

            F main = f(env, print, Names.result).label("main");
            F dec = f(env, nop, Names.a).label("dec");
            F sub = f(env, Sub.sub, Names.left, Names.right).label("sub");
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
    }

    @Test
    public void testIf() {

        ReRun.InitialRun rInit = runAndCheck(_2, n -> {
            F max = createMax(n);
            Gateway<Integer> sync = getSynchronized(n, max);
            sync.param(Names.left, 2);
            sync.param(right, 1);
            return sync;
        });

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> log = rInit.events().subList(0, rInit.events().size() - 2);

        ReRun.reReReReRunAndCheck(rInit.exId(), (id, n) -> {
            F max = createMax(n);
            Gateway<Integer> sync = getSynchronized(id, n, max);
            sync.param(Names.left, 2);
            sync.param(right, 1);
            return sync;
        }, log, _2);
    }

    /*
     * max(left, right) = if(left > right) left else right
     *
     * */
    private F createMax(Env env) {
        F max = f(env, nop, Names.left, Names.right).label("max");
        If iff = iff(env).label("if");
        F gt = f(env, Gt.gt, Names.left, Names.right).returnAs(Names.condition).label("gt");

        max.addPropagation(Names.left, iff);
        max.addPropagation(Names.right, iff);

        //TODO possible that nothing is stashed? Also in useFVar?
        iff.addPropagation(COND_CONDITION, Names.left, gt);
        iff.addPropagation(COND_CONDITION, Names.right, gt);
        iff.addPropagation(COND_TRUE_BRANCH, Names.left, Names.result, iff);
        iff.addPropagation(COND_FALSE_BRANCH, Names.right, Names.result, iff);

        return max;
    }

    @Test
    public void testRecSum() {
        final AtomicInteger i1 = new AtomicInteger(0);

        try (Node env = createNode()) {
            F main = createRecSum(env);

            env.start();

            Gateway.of(Integer.class, main, env).param(Names.a, 3).callAsync(i1::addAndGet);

            Concurrent.await(() -> i1.get() > 0);

            assertEquals(Integer.valueOf(6), Integer.valueOf(i1.get()));

            System.out.println(env.getMaxExCount() + " exs used for recursive geometrical sum");
        }
    }

    @Test
    public void testRecSumSimultaneously() {
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);

        try (Node env = createNode()) {
            F main = createRecSum(env);

            env.start();

            Gateway.of(Integer.class, main, env).param(Names.a, 3).callAsync(i1::addAndGet);
            Gateway.of(Integer.class, main, env).param(Names.a, 150).callAsync(i2::addAndGet);

            Concurrent.await(() -> i1.get() > 0 && i2.get() > 0);

            assertEquals(Integer.valueOf(6), Integer.valueOf(i1.get()));
            assertEquals(Integer.valueOf(11325), Integer.valueOf(i2.get()));

            System.out.println(env.getMaxExCount() + " exs used for recursive geometrical sum");
        }
    }




    @Test
    public void testTailRecSum() {
        final AtomicInteger i1 = new AtomicInteger(0);
        SimpleListEventLog log = new SimpleListEventLog();
        try (Node env = new Node(Address.localhost, log, log)) {
            env.start();
            F s1 = createTailRecSum(env);

            Gateway.of(Integer.class, s1, env).param(Names.a, 3).param(Names.c, 3).callAsync(i1::addAndGet);

            Concurrent.await(() -> i1.get() > 0);
            assertEquals(Integer.valueOf(6), Integer.valueOf(i1.get()));
            System.out.println(env.getMaxExCount() + " exs used simultaneously for 3 tail recursive geometrical sums of 153");
        }
    }


    @Test
    public void testTailRecSumAsync() {
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);
        final AtomicInteger i3 = new AtomicInteger(0);
        SimpleListEventLog log = new SimpleListEventLog();
        try (Node env = new Node(Address.localhost, log, log)) {
            env.start();
            F s1 = createTailRecSum(env);
            F s2 = createTailRecSum(env);
            F s3 = createTailRecSum(env);

            Gateway.of(Integer.class, s1, env).param(Names.a, 153).param(Names.c, 153).callAsync(i1::addAndGet);
            Gateway.of(Integer.class, s2, env).param(Names.a, 153).param(Names.c, 153).callAsync(i2::addAndGet);
            Gateway.of(Integer.class, s3, env).param(Names.a, 153).param(Names.c, 153).callAsync(i3::addAndGet);

            Concurrent.await(() -> i1.get() > 0 && i2.get() > 0 && i3.get() > 0);
            assertEquals(Integer.valueOf(11781), Integer.valueOf(i1.get()));
            assertEquals(Integer.valueOf(11781), Integer.valueOf(i2.get()));
            assertEquals(Integer.valueOf(11781), Integer.valueOf(i3.get()));
            System.out.println(env.getMaxExCount() + " exs used simultaneously for 3 tail recursive geometrical sums of 153");
        }
    }

    @Test
    public void testTailReReReReReRecursion() {
        ReRun.InitialRun rInit = runAndCheck(_3, n -> {
            Gateway<Integer> sync = getSynchronized(n, createTailRecSum(n));
            sync.param(Names.a, 2);
            sync.param(Names.c, 2);
            return sync;
        });

        // removal of recursion elements goes on after the recursive function terminated, so nothing will happen if
        // the log isn't truncated to a state before that termination
        List<Hydratable> log = new ArrayList<>();
        for (Hydratable h : rInit.events()) {
            if (h instanceof ValueProcessedEvent e && e.value.getName().equals(result) && e.ex.getTemplate().isTailRecursive())
                break;
            log.add(h);
        }

        ReRun.reReReReRunAndCheck(rInit.exId(), (id, n) -> getSynchronized(id, n, createTailRecSum(n)), log, _3);
    }


    @Test
    public void testQuicksort() {
        Node env = createNode();

        F main = createQuicksort(env);

        env.start();
        try {
            testFor(env, main, list(), list());
            testFor(env, main, list(1), list(1));
            testFor(env, main, list(1, 2), list(1, 2));
            testFor(env, main, list(2, 1), list(1, 2));
            testFor(env, main, list(9, 0, 8, 1, 7, 2, 6, 3, 5, 4), list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

            ArrayList<Integer> randList = randomList(150);
            ArrayList<Integer> randListSorted = new ArrayList<>(randList);
            randListSorted.sort(Integer::compareTo);
            testFor(env, main, randList, randListSorted);
        } finally {
            env.close();
        }
    }


    //@Test
    public void testQuickReReReReReSort() {
        for (int i = 0; i < 5; i++) {
            testQuickReSort();
        }
    }

    @Test
    public void testQuickReSort() {
        ArrayList<Integer> initial = randomList(3);
        ArrayList<Integer> randListSorted = new ArrayList<>(initial);
        randListSorted.sort(Integer::compareTo);
        ArrayList<Integer> expected = randListSorted;

        ReRun.InitialRun rInit = runAndCheck(expected, (n) -> getSynchronized(List.class, n, createQuicksort(n)).param("list", initial));

        // truncate log, so that last result will get processed again in any case
        List<Hydratable> log = rInit.events().subList(0, rInit.events().size() - 2);

        ReRun.reReReReRunAndCheck(rInit.exId(), (id, n) -> getSynchronized(List.class, id, n, createQuicksort(n)), log, expected);
    }


    @Test
    public void testFilter() {
        Node env = createNode();

        F main = createFilterTest(env);

        env.start();
        testFor(env, main, list(), list());
        testFor(env, main, list(1), list());
        testFor(env, main, list(1, 2, 3), list());
        testFor(env, main, list(1, 2, 3, 4, 5), list(4, 5));
        testFor(env, main, list(5), list(5));
        env.close();
    }

    /**
     * returns a function without parameters, a ping example
     */
    private F createFilterTest(Env env) {
        F main = f(env, nop).label("filter");

        String const3 = "const:3";
        F gt = f(env, Gt.gt, Names.left, Names.right).label("gt?");
        F createPredicate = new FCreatePartiallyAppliedFunction(env, gt, Names.right).returnAs(predicate).label("createPredicate");

        main.addPropagation(list, ping, createPredicate);
        createPredicate.addPropagation(ping, CONST(env, 3).returnAs(Names.right).label(const3));

        F filter = createFilter(env);
        F callFilter = new FCall(env, filter).label("callFilter initially");

        main.addPropagation(predicate, callFilter);
        main.addPropagation(list, callFilter);
        return main;
    }

    private void testFor(Env env, F main, ArrayList<Integer> source, ArrayList<Integer> expected) {
        assertEquals(expected, getSynchronized(List.class, env, main).param(list, source).call());
        assertEquals(0, ((Node) env).getCrankCount());
        System.out.printf("Tested %s input size: %d max executions used simultaneously:%d\n", main.getLabel(), source.size(), env.getMaxExCount());
    }

    private Node createNode() {
        return new Node(Address.localhost, false, false);
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


    @Test
    public void printf() {
        try (var v = new FVisualizer(createTailRecSum(createNode()))) {
            v.render();
        }
    }

    private void resumeComputation(Supplier<_F> getF) {
        Node env = createNode();
        getF.get();
        env.setDelay(1);
        env.start(true);
        Concurrent.sleep(50000);
        env.close();
    }
}
