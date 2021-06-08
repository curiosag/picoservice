package micro;

import micro.event.ValueProcessedEvent;
import micro.event.eventlog.memeventlog.SimpleListEventLog;
import micro.gateway.Gateway;
import micro.primitives.Action;
import micro.primitives.Constant;
import micro.primitives.Gt;
import micro.primitives.Sub;
import micro.visualize.FVisualizer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static micro.Algorithm.*;
import static micro.Names.*;
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
        useFVar.addPropagation(Names.a, Names.left, callSubtract);
        useFVar.addPropagation(Names.b, Names.right, callSubtract);

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

    private F createPFApp(Env env) {
        F main = f(env, nop, Names.output).label("main");

        F sub = f(env, Sub.sub, Names.left, Names.right).label("sub");
        F usePartial = f(env, nop, Names.a, Names.b).label("usePartial");

        F createDec = new FCreatePartiallyAppliedFunction(env, sub, Names.right).returnAs(paramFVar).label("createDec");
        F callDec = new FunctionalValueDefinition(env, paramFVar, Names.left).label("callDec");

        usePartial.addPropagation(Names.b, Names.right, createDec);
        usePartial.addPropagation(paramFVar, callDec);
        usePartial.addPropagation(Names.a, Names.left, callDec);

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
            env.start();
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

    @Test
    public void testRecSum() {
        final AtomicInteger i1 = new AtomicInteger(0);

        try (Node env = createNode()) {
            F main = createRecSum(env);

            env.start();

            Gateway.of(Integer.class, main, env).param(Names.a, 3).callAsync(i1::addAndGet);

            Concurrent.await(() -> i1.get() > 0);

            assertEquals(Integer.valueOf(6), Integer.valueOf(i1.get()));

            System.out.println(env.getMaxExCount() + " exs used max for recursive geometrical sum");
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
            System.out.println(env.getMaxExCount() + " exs used max for tail recursive geometrical sum");
        }
    }

    @Test
    public void testParallelRecSums() {
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);
        final AtomicInteger i3 = new AtomicInteger(0);
        final AtomicInteger i4 = new AtomicInteger(0);

        try (Node env = createNode()) {
            F main = createRecSum(env);
            F mainT = createTailRecSum(env);

            env.start();

            Gateway.of(Integer.class, main, env).param(Names.a, 149).callAsync(i1::addAndGet);
            Gateway.of(Integer.class, main, env).param(Names.a, 150).callAsync(i2::addAndGet);
            Gateway.of(Integer.class, mainT, env).param(Names.a, 149).param(Names.c, 149).callAsync(i3::addAndGet);
            Gateway.of(Integer.class, mainT, env).param(Names.a, 150).param(Names.c, 150).callAsync(i4::addAndGet);

            Concurrent.await(() -> i1.get() > 0 && i2.get() > 0&& i3.get() > 0&& i4.get() > 0);

            assertEquals(Integer.valueOf(11175), Integer.valueOf(i1.get()));
            assertEquals(Integer.valueOf(11325), Integer.valueOf(i2.get()));
            assertEquals(Integer.valueOf(11175), Integer.valueOf(i3.get()));
            assertEquals(Integer.valueOf(11325), Integer.valueOf(i4.get()));

            System.out.println(env.getMaxExCount() + " exs used for recursive geometrical sum");
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
        System.out.printf("Tested %s input size: %d max %d executions used simultaneously by %d threads \n", main.getLabel(), source.size(), env.getMaxExCount(), ((Node) env).getThreadsUsed());
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
        try (var n = createNode()) {
            render(createRecSum(n));
            render(createTailRecSum(n));
            render(createFilter(n));
            render(createQuicksort(n));
        }
    }

    private void render(F f) {
        try (var v = new FVisualizer(f)) {
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
}
