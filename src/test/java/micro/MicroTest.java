package micro;

import micro.If.If;
import micro.primitives.*;
import micro.primitives.Lists.*;
import nano.ingredients.Name;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static micro.If.If.iff;
import static micro.Names.*;
import static micro.PropagationType.*;
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
    private Node node;


    @Test
    public void testLet() {
        node = new Node(address, true);
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

        F f = f(nop, Names.a).returnAs(Names.output).label("f");
        F mul = f(mul(), Names.left, Names.right).label("mul").returnAs(Names.b);
        F add = f(add(), Names.left, Names.right).label("add");

        f.addPropagation(ping, CONST(2).returnAs(Names.a).label("const:2"));
        f.addPropagation(Names.a, Names.left, mul);
        f.addPropagation(Names.a, Names.right, mul);

        f.addPropagation(Names.a, Names.left, add);
        f.addPropagation(Names.b, Names.right, add);

        F main = f(resultListener, Names.output).label("main");
        main.addPropagation(ping, f);

        _Ex ex = main.createExecution(node.getTop());

        node.start();
        try {
            ex.receive(Value.of(ping, 0, node.getTop()));
            Concurrent.await(() -> !result.isEmpty());
            assertEquals(6, result.get().get(0).get());
        } finally {
            node.close();
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

        node = new Node(address, true);
        try {
            node.start();
            F main = f(print(), Names.result).label("main");
            F add = f(add(), Names.left, Names.right).label("add");

            main.addPropagation(Names.a, Names.left, add);
            main.addPropagation(Names.b, Names.right, add);

            CallSync<Integer> sync = CallSync.of(Integer.class, main);
            sync.param(Names.a, 1);
            sync.param(Names.b, 2);

            assertEquals(_3, sync.call());
        } finally {
            node.close();
        }

    }

    @Test
    public void testSimpleFunc_usingFCall() {
        node = new Node(address, true);
        try {
            node.start();
            F main = f(print(), Names.result).label("main");
            F add = f(add(), Names.left, Names.right).label("add");

            // FCall is redundant here, main could interact with add directly
            F callAdd = new FCall(node, add).returnAs(result).label("callAdd");

            main.addPropagation(Names.a, Names.left, callAdd);
            main.addPropagation(Names.b, Names.right, callAdd);

            CallSync<Integer> sync = CallSync.of(Integer.class, main);
            sync.param(Names.a, 1);
            sync.param(Names.b, 2);

            assertEquals(_3, sync.call());
        } finally {
            node.close();
        }
    }


    /*
        function add(left,right)=left+right
        function mul(left,right)=left*right
        function trisum (a,b,c) = add(add(a, b), c)
        function trimul (a,b,c) = mul(mul(a, b), c)
        function calc(a,b,c= sum(trisum(1,2,3), trimul(1,2,3))

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

    private F createTriOp(F binOp, String label) {
        F op1 = new FCall(node, binOp).label(label + "_1");
        F op2 = new FCall(node, binOp).label(label + "_2");
        F triOp = f(nop, Names.a, Names.b, Names.c).label(label + "_triop");

        triOp.addPropagation(Names.a, op1);
        triOp.addPropagation(Names.b, op1);
        triOp.addPropagation(Names.c, Names.right, op1);

        op1.addPropagation(Names.a, Names.left, op2);
        op1.addPropagation(Names.b, Names.right, op2);

        op2.returnAs(Names.left);
        op1.returnAs(Names.result);

        return triOp;
    }

    private F createCalc() {
        F calc = f(nop, Names.result).label("calc");

        // single instances of add and mul, accessed via FCalls
        F add = f(add(), Names.left, Names.right).label("add").returnAs(Names.result);
        F mul = f(mul(), Names.left, Names.right).label("mul").returnAs(Names.result);
        F triSum = createTriOp(add, "triSum").returnAs(Names.left);
        F triMul = createTriOp(mul, "triMul").returnAs(Names.right);

        F callAdd = new FCall(node, add).returnAs(Names.result);
        calc.addPropagation(Names.a, callAdd);
        calc.addPropagation(Names.b, callAdd);
        calc.addPropagation(Names.c, callAdd);

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
        node = new Node(address, true);
        F calc = createCalc();

        node.start();
        try {
            CallSync<Integer> sync = CallSync.of(Integer.class, calc);
            sync.param(Names.a, 1);
            sync.param(Names.c, 2);
            sync.param(Names.b, 3);

            assertEquals(Integer.valueOf(12), sync.call());
        } finally {
            node.close();
        }
    }

    @Test
    public void testCalcAsync() {
        node = new Node(address, true);
        F calc = createCalc();
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);

        node.start();
        try {

            CallAsync<Integer> call1 = CallAsync.of(Integer.class, calc, i1::addAndGet);
            CallAsync<Integer> call2 = CallAsync.of(Integer.class, calc, i2::addAndGet);

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
            node.close();
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
        node = new Node(address, true);

        ResultCollector result = new ResultCollector();
        Action resultListener = new Action(i -> result.set(i.values()));

        F main = f(resultListener, Names.output).label("main");

        F sub = f(subInt(), Names.left, Names.right).label("sub");
        F useFVar = f(nop, Names.a, Names.b).label("useFVar").returnAs(Names.output);

        // a plain functional value is a partially applied function with no partial parameters
        F createSubtract = new FCreatePartiallyAppliedFunction(node, sub).returnAs(paramFVar).label("createSubtract");
        F callSubtract = new FCallByFunctionalValue(node, paramFVar, Names.left, Names.right).label("callSubtract");

        useFVar.addPropagation(Names.a, ping, createSubtract);
        useFVar.addPropagation(paramFVar, callSubtract);
        useFVar.addPropagation(Name.a, Names.left, callSubtract);
        useFVar.addPropagation(Name.b, Names.right, callSubtract);

        main.addPropagation(Names.a, useFVar);
        main.addPropagation(Names.b, useFVar);

        _Ex ex = main.createExecution(node.getTop());

        node.start();

        try {
            ex.receive(Value.of(Names.a, 2, node.getTop()));
            ex.receive(Value.of(Names.b, 1, node.getTop()));

            Concurrent.await(() -> !result.isEmpty());
            assertEquals(1, result.get().get(0).get());
        } finally {
            node.close();
        }

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
        node = new Node(address, true);
        ResultCollector result = new ResultCollector();
        Action resultListener = new Action(i -> result.set(i.values()));

        F main = f(resultListener, Names.output).label("main");

        F sub = f(subInt(), Names.left, Names.right).label("sub");
        F usePartial = f(nop, Names.a, Names.b).returnAs(Names.output).label("usePartial");

        F createDec = new FCreatePartiallyAppliedFunction(node, sub, Names.right).returnAs(paramFVar).label("createDec");
        F callDec = new FCallByFunctionalValue(node, paramFVar, Names.left).label("callDec");

        usePartial.addPropagation(Names.b, Names.right, createDec);
        usePartial.addPropagation(paramFVar, callDec);
        usePartial.addPropagation(Name.a, Names.left, callDec);

        main.addPropagation(Names.a, usePartial);
        main.addPropagation(Names.b, usePartial);

        _Ex ex = main.createExecution(node.getTop());

        node.start();

        ex.receive(Value.of(Names.a, 2, node.getTop()));
        ex.receive(Value.of(Names.b, 1, node.getTop()));

        Concurrent.await(() -> !result.isEmpty());
        assertEquals(1, result.get().get(0).get());

        node.close();
    }

    @Test
    public void testConst() {
        node = new Node(address, true);

        F main = f(print(), Names.result).label("main");
        F dec = f(nop, Names.a).label("dec");
        F sub = f(subInt(), Names.left, Names.right).label("sub");
        F one = f(new Const(1)).returnAs(Names.right).label("const:one");

        main.addPropagation(ping, dec);
        dec.addPropagation(ping, sub);
        sub.addPropagation(ping, one);

        main.addPropagation(Names.a, dec);
        dec.addPropagation(Names.a, Names.left, sub);
        _Ex TOP = node.getTop();
        _Ex ex = main.createExecution(TOP);
        ex.receive(Value.of(ping, 0, TOP));
        ex.receive(Value.of(Names.a, 1, TOP));
    }

    @Test
    public void testIf() {
        node = new Node(address, true);
        try {
            node.start();
            F main = f(print(), Names.result).label("main");
            F max = f(nop, Names.left, Names.right).label("max");
            If iff = iff(node).label("if");
            F gt = f(gt(), Names.left, Names.right).returnAs(Names.condition).label("gt");

            main.addPropagation(Names.left, max);
            main.addPropagation(Names.right, max);

            max.addPropagation(Names.left, iff);
            max.addPropagation(Names.right, iff);

            iff.addPropagation(CONDITION, Names.left, gt);
            iff.addPropagation(CONDITION, Names.right, gt);
            iff.addPropagation(TRUE_BRANCH, Names.left, Names.result, iff);
            iff.addPropagation(FALSE_BRANCH, Names.right, Names.result, iff);


            _Ex TOP = node.getTop();
            _Ex ex = main.createExecution(TOP);
            ex.receive(Value.of(Names.left, 1, TOP));
            ex.receive(Value.of(Names.right, 2, TOP));

            CallSync<Integer> sync = CallSync.of(Integer.class, main);
            sync.param(Names.left, 2);
            sync.param(right, 1);

            assertEquals(Integer.valueOf(2), sync.call());
        } finally {
            node.close();
        }
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
        node = new Node(address, true);
        F main = createRecSum();
        node.setDelay(1);
        node.start();

        _Ex m1 = node.getExecution(main);
        _Ex m2 = node.getExecution(main);

        m1.receive(Value.of(Names.a, 50, m1.returnTo()));
        m2.receive(Value.of(Names.a, 50, m1.returnTo()));

        Concurrent.sleep(8000);
        node.close();
    }

    @Test
    public void testResumeSimpleRecursion() {
        resumeComputation(this::createRecSum);
    }

    private F createRecSum() {
        F main = f(print(), Names.result).label("main");
        F geo = f(nop, Names.a).label("geo");
        If iff = iff(node).label("if");

        main.addPropagation(Names.a, geo);
        geo.addPropagation(Names.a, iff);

        // condition
        F eq = f(eq(), Names.left, Names.right).returnAs(Names.condition).label("eq");

        iff.addPropagation(CONDITION, Names.a, Names.left, eq);
        eq.addPropagation(Names.left, ping, CONST(0).returnAs(Names.right).label("zero:eq"));
        // onTrue
        iff.addPropagation(TRUE_BRANCH, Names.a, ping, CONST(0).label("zero:ontrue"));
        // onFalse
        F block_else = f(nop).label("block_else");
        iff.addPropagation(FALSE_BRANCH, Names.a, block_else);
        // let next_a = a - 1
        String next_a = "next_a";
        F sub = f(subInt(), Names.left, Names.right).returnAs(next_a).label("sub");
        block_else.addPropagation(Names.a, Names.left, sub);
        sub.addPropagation(Names.left, ping, CONST(1).returnAs(Names.right).label("one"));
        // a + geo(next_a);
        F add = f(add(), Names.left, Names.right).label("add");
        F geoReCall = new FCall(node, geo).returnAs(Names.right).label("geoCallR");

        block_else.addPropagation(Names.a, Names.left, add);
        block_else.addPropagation(next_a, add);
        add.addPropagation(next_a, Names.a, geoReCall);
        return main;
    }

    @Test
    public void testQuicksort() {
        node = new Node(address, true);

        ResultCollector result = new ResultCollector();
        F main = createQuicksortCall(result);

        node.start();
        try {
            testFor(result, main, list(), list());
            testFor(result, main, list(1), list(1));
            testFor(result, main, list(1, 2), list(1, 2));
            testFor(result, main, list(2, 1), list(1, 2));
            testFor(result, main, list(9, 0, 8, 1, 7, 2, 6, 3, 5, 4), list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

            ArrayList<Integer> randList = randomList(100);
            ArrayList<Integer> randListSorted = new ArrayList<>(randList);
            randListSorted.sort(Integer::compareTo);
            testFor(result, main, randList, randListSorted);
            System.out.println("Max exid used: " + (node.getNextObjectId() - 1));
        } finally {
            node.close();
        }
    }

    @Test
    public void testSuspendQuicksort() {
        node = new Node(address, true);

        ResultCollector result = new ResultCollector();
        F main = createQuicksortCall(result);

        node.start();

        ArrayList<Integer> actual = list(9, 0, 8, 1, 7, 2, 6, 3, 5, 4);
        _Ex ex = node.getExecution(main, node.getTop());
        ex.receive(Value.of(Names.list, actual, node.getTop()));
        Concurrent.sleep(5000);
        node.close();

        Concurrent.sleep(250);
        node.stop();
        Concurrent.sleep(500);
        node.close();
    }

    @Test
    public void testResumeQuicksort() {
        node = new Node(address, false);

        ResultCollector result = new ResultCollector();
        F main = createQuicksortCall(result);

        node.recover();
        node.start();

        Concurrent.await(() -> !result.isEmpty());
        Assert.assertEquals(list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), result.get().get(0).get());
        node.close();
    }

    @Test
    public void testSuspendResumeQuicksort() {
        testSuspendQuicksort();
        testResumeQuicksort();
    }

    private F createQuicksortCall(ResultCollector result) {
        Action resultListener = new Action(i -> result.set(i.values()));

        F main = f(resultListener, Names.output).label("main");
        F quicksort = createQuicksort();
        F callQuicksort = new FCall(node, quicksort).returnAs(Names.output).label("callQuicksort initially");
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

    private F createQuicksort() {
        String predicateTestGt = "predicateTestGt";
        String predicateTestLteq = "predicateTestLteq";
        String filteredGt = "filteredGt";
        String filteredLteq = "filteredLteq";
        String sortedGt = "sortedGt";
        String sortedLteq = "sortedLteq";
        String consed = "consed";

        F quicksort = new F(node, nop, list).label("QUICKSORT(list)");
        If if_listEmpty = iff(node).label("if:listEmpty");
        quicksort.addPropagation(list, if_listEmpty);

        F isEmpty = new F(node, new IsEmpty(), list).returnAs(condition).label("isEmpty");
        if_listEmpty.addPropagation(CONDITION, list, isEmpty);
        F constEmptyList = new F(node, new Const(Collections.emptyList()), ping).label("const:emptylist()");
        if_listEmpty.addPropagation(TRUE_BRANCH, list, ping, constEmptyList);

        F block_else = f(nop).label("block_else");
        if_listEmpty.addPropagation(FALSE_BRANCH, list, block_else);

        F head = new F(node, new Head(), list).returnAs(Names.head).label("head()");
        F tail = new F(node, new Tail(), list).returnAs(Names.tail).label("tail()");
        block_else.addPropagation(list, head);
        block_else.addPropagation(list, tail);

        F gt = f(Gt.gt(), Names.left, Names.right).label("gt?");
        F createTestGt = new FCreatePartiallyAppliedFunction(node, gt, Names.right).returnAs(predicateTestGt).label("createTestGt");
        block_else.addPropagation(Names.head, right, createTestGt);

        F lteq = f(Lteq.lteq(), Names.left, Names.right).label("lteq?");
        F createTestLteq = new FCreatePartiallyAppliedFunction(node, lteq, Names.right).returnAs(predicateTestLteq).label("createTestLteq");
        block_else.addPropagation(Names.head, right, createTestLteq);

        F filter = createFilter();
        F filterCallGt = new FCall(node, filter).returnAs(filteredGt).label("filterCallGt");
        F filterCallLteq = new FCall(node, filter).returnAs(filteredLteq).label("filterCallLteq");

        block_else.addPropagation(predicateTestGt, predicate, filterCallGt);
        block_else.addPropagation(predicateTestLteq, predicate, filterCallLteq);
        block_else.addPropagation(Names.tail, list, filterCallGt);
        block_else.addPropagation(Names.tail, list, filterCallLteq);

        F qsortRecallGt = new FCall(node, quicksort).returnAs(sortedGt).label("qsortRecallGt");
        F qsortRecallLteq = new FCall(node, quicksort).returnAs(sortedLteq).label("qsortRecallLteq");
        block_else.addPropagation(filteredGt, Names.list, qsortRecallGt);
        block_else.addPropagation(filteredLteq, Names.list, qsortRecallLteq);

        F cons = new F(node, new Cons(), element, list).returnAs(consed).label("cons");
        block_else.addPropagation(Names.head, Names.element, cons);
        block_else.addPropagation(sortedGt, list, cons);

        F concat = new F(node, new Concat(), left, right).label("concat");
        block_else.addPropagation(consed, right, concat);
        block_else.addPropagation(sortedLteq, Names.left, concat);


        return quicksort;
    }

    @Test
    public void testFilter() {
        node = new Node(address, true);
        ResultCollector result = new ResultCollector();
        Action resultListener = new Action(i -> result.set(i.values()));

        F main = createFilterTest(resultListener);

        node.start();
        testFor(result, main, list(), list());
        testFor(result, main, list(1), list());
        testFor(result, main, list(1, 2, 3), list());
        testFor(result, main, list(1, 2, 3, 4, 5), list(4, 5));
        testFor(result, main, list(5), list(5));
        node.close();
    }

    private F createFilterTest(Action resultListener) {
        F main = f(resultListener, Names.output).label("main");

        String const3 = "const:3";
        F gt = f(Gt.gt(), Names.left, Names.right).label("gt?");
        F createPredicate = new FCreatePartiallyAppliedFunction(node, gt, Names.right).returnAs(predicate).label("createPredicate");

        main.addPropagation(list, ping, createPredicate);
        createPredicate.addPropagation(ping, CONST(3).returnAs(Names.right).label(const3));

        F filter = createFilter();
        F callFilter = new FCall(node, filter).returnAs(Names.output).label("callFilter initially");

        main.addPropagation(predicate, callFilter);
        main.addPropagation(list, callFilter);
        return main;
    }

    @Test
    public void testSuspendFilterTest() {
        node = new Node(address, true);
        ResultCollector result = new ResultCollector();
        Action resultListener = new Action(i -> result.set(i.values()));

        F main = createFilterTest(resultListener);

        node.start();
        node.setDelay(5);

        ArrayList<Integer> source = list(1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        _Ex ex = node.getExecution(main, node.getTop());
        ex.receive(Value.of(Names.list, source, node.getTop()));

        Concurrent.sleep(3000);
        node.stop();
        Concurrent.sleep(1000);
        node.close();
    }

    @Test
    public void testResumeFilterTest() {
        node = new Node(address, false);
        ResultCollector result = new ResultCollector();

        resumeComputation(() -> {
            Action resultListener = new Action(i -> result.set(i.values()));
            return createFilterTest(resultListener);
        });
    }

    private void testFor(ResultCollector resultCollector, F main, ArrayList<Integer> source, ArrayList<Integer> expected) {
        resultCollector.clear();
        _Ex ex = node.getExecution(main, node.getTop());
        ex.receive(Value.of(Names.list, source, node.getTop()));
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

    private F createFilter() {
        String tailFiltered = "tailFiltered";

        F filter = new F(node, nop, predicate, list);
        If if_listEmpty = iff(node).label("**if:listEmpty");
        filter.addPropagation(list, if_listEmpty);
        filter.addPropagation(predicate, if_listEmpty);

        F isEmpty = new F(node, new IsEmpty(), list).returnAs(condition).label("**isEmpty");
        if_listEmpty.addPropagation(CONDITION, list, isEmpty);
        F constEmptyList = new F(node, new Const(Collections.emptyList()), ping).label("**const:emptylist()");
        if_listEmpty.addPropagation(TRUE_BRANCH, list, ping, constEmptyList);

        F block_else = f(nop).label("**block_else");
        if_listEmpty.addPropagation(FALSE_BRANCH, list, block_else);
        if_listEmpty.addPropagation(FALSE_BRANCH, predicate, block_else);

        F head = new F(node, new Head(), list).returnAs(Names.head).label("**head()");
        F tail = new F(node, new Tail(), list).returnAs(Names.tail).label("**tail()");
        F filterReCall = new FCall(node, filter).returnAs(tailFiltered).label("**filterReCall");

        If if_predicate = iff(node).label("**if:predicate");
        block_else.addPropagation(list, head);
        block_else.addPropagation(list, tail);
        block_else.addPropagation(Names.tail, Names.list, filterReCall);
        block_else.addPropagation(predicate, filterReCall);

        block_else.addPropagation(predicate, if_predicate);
        block_else.addPropagation(Names.head, if_predicate);
        block_else.addPropagation(tailFiltered, if_predicate);

        // TODO: underlying partial function takes left/right, right is already applied, left needs to be supplied
        // would be nice to have a remapping somewhere for the remaining 1 parameter, "left" -> "argument" or so
        F callPredicate = new FCallByFunctionalValue(node, predicate, Names.left).returnAs(condition).label("**callPredicateByFVal");

        if_predicate.addPropagation(CONDITION, predicate, callPredicate);
        if_predicate.addPropagation(CONDITION, Names.head, Names.left, callPredicate);

        F cons = new F(node, new Cons(), element, list).label("**cons");
        if_predicate.addPropagation(TRUE_BRANCH, Names.head, element, cons);
        if_predicate.addPropagation(TRUE_BRANCH, tailFiltered, Names.list, cons);

        if_predicate.addPropagation(FALSE_BRANCH, tailFiltered, new F(node, new Val(), tailFiltered).label("**VAL:tailFiltered"));

        return filter;
    }

    private F CONST(Object i) {
        return F.f(node, new Const(i), ping);
    }

    private F f(Primitive primitive, String... params) {
        return F.f(node, primitive, params);
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
        node = new Node(address, false);
        getF.get();
        node.setDelay(1);
        node.recover();
        node.start();
        Concurrent.sleep(50000);
        node.close();
    }
}
