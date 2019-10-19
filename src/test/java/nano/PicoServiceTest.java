package nano;

import nano.implementations.nativeImpl.BinOps;
import nano.ingredients.*;
import nano.ingredients.gateway.Execution;
import nano.ingredients.gateway.Gateway;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static nano.Int.Int;
import static nano.implementations.Filter.filterSignature;
import static nano.implementations.Quicksort.getQuicksortSignature;
import static nano.implementations.nativeImpl.BinOps.add;
import static nano.ingredients.Action.action;
import static nano.ingredients.AsyncStuff.await;
import static nano.ingredients.CallSync.sync;
import static nano.ingredients.Const.constant;
import static nano.ingredients.FunctionCall.functionCall;
import static nano.ingredients.FunctionSignature.functionSignature;
import static nano.ingredients.Iff.iff;
import static nano.ingredients.Message.message;
import static nano.ingredients.Nop.nop;
import static nano.ingredients.Origin.origin;
import static nano.ingredients.PartialFunctionApplication.partialApplication;
import static nano.ingredients.RunProperty.*;
import static nano.ingredients.gateway.Gateway.intGateway;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PicoServiceTest {
    private static Logger logger = Logger.getLogger("ServiceTest");
    private final static ArrayList<Integer> emptyList = new ArrayList<>();

    private static final Integer _6 = 6;
    private static final Integer _5 = 5;
    private static final Integer _4 = 4;
    private static final Integer _3 = 3;
    private static final Integer _2 = 2;
    private static final Integer _1 = 1;
    private static final Integer _0 = 0;


    @After
    public void after() {
        Ensemble.terminate();
        Ensemble.reset();
    }

    @Test
    public void testLambda() {
        /*
                function mul(a) = a * 2
                function double(func, a) = func(a) + func(a)

                check(double(mul, a))
         */

        // function mul(a) = a * 2

        Function<Integer> mul = BinOps.mul().constant(Name.rightArg, 2);
        FunctionSignature<Integer> functionSignatureMul = functionSignature(mul).paramList(Name.a);
        functionSignatureMul.propagate(Name.a, Name.leftArg, mul);

        //function double(func, a) = func(a) + func(a)
        Function<Integer> add = add();

        FunctionSignature<Integer> functionSignatureDouble = functionSignature(add).paramList(Name.a, Name.func);
                ;
        FunctionStub<Integer> stubFuncLeft = FunctionStub.of(Name.func);
        stubFuncLeft.returnTo(add, Name.leftArg);
        FunctionStub<Integer> stubFuncRight = FunctionStub.of(Name.func);
        stubFuncRight.returnTo(add, Name.rightArg);


        functionSignatureDouble.propagate(Name.a, Name.a, add);
        add.propagate(Name.a, Name.a, stubFuncLeft);
        add.propagate(Name.a, Name.a, stubFuncRight);
        functionSignatureDouble.propagate(Name.func, Name.func, add);
        add.propagate(Name.func, Name.func, stubFuncLeft);
        add.propagate(Name.func, Name.func, stubFuncRight);

        // check(double(mul, a))
        FunctionCall<Integer> functionCallDouble = functionCall(functionSignatureDouble);
        Int result = Int(0);
        Action resultListener = action((Consumer<Message> & Serializable) i -> result.setValue((Integer) i.getValue()));
        resultListener.param(Name.result);
        functionCallDouble.returnTo(resultListener, Name.result);

        functionSignatureMul.label("MUL");
        functionSignatureDouble.label("DOUBLE");
        functionCallDouble.label("double");
        stubFuncLeft.label("stubLeft");
        stubFuncRight.label("stubRight");

        Origin origin = Origin.origin(resultListener);
        functionCallDouble.tell(message(Name.func, functionSignatureMul, origin));
        functionCallDouble.tell(message(Name.a, _3, origin));
        await(() -> result.value != 0);
        assertEquals(Integer.valueOf(12), result.value);

    }

    @Test
    public void testErrorHandling() {

        /*
                function div(a) = 8 / a
                function apply(func, a) = func(a)

                check(apply(div, a))
         */

        Function<Integer> div = BinOps.div().constant(Name.leftArg, 8);
        div.label(" div ");
        FunctionSignature<Integer> functionSignatureDiv = functionSignature(div);
        functionSignatureDiv.propagate(Name.a, Name.rightArg, div);
        functionSignatureDiv.label("DIV");

        //function apply(func, a) = func(a)
        FunctionStub<Integer> stubFunc = FunctionStub.of(Name.func);
        stubFunc.label("funcStub");

        FunctionSignature<Integer> functionSignatureApply = functionSignature(stubFunc);
        functionSignatureApply.propagate(Name.a, Name.a, stubFunc);
        functionSignatureApply.propagate(Name.func, Name.func, stubFunc);
        functionSignatureApply.label("APPLY");

        // check(apply(div, a))
        FunctionCall<Integer> functionCallApply = functionCall(functionSignatureApply);
        functionCallApply.label("functionCallApply");

        AtomicReference<Message> result = new AtomicReference<>(null);

        Action resultListener = action((Consumer<Message> & Serializable) result::set);
        resultListener.param(Name.result);
        resultListener.param(Name.error);
        functionCallApply.returnTo(resultListener, Name.result);

        Origin origin = Origin.origin(resultListener, 0L);
        functionCallApply.tell(message(Name.func, functionSignatureDiv, origin));

        result.set(null);
        functionCallApply.tell(message(Name.a, _2, origin));
        await(() -> result.get() != null);
        assertEquals(_4, result.get().getValue());

        origin = Origin.origin(resultListener, 1L);
        result.set(null);
        functionCallApply.tell(message(Name.func, functionSignatureDiv, origin));
        functionCallApply.tell(message(Name.a, _0, origin));
        await(() -> result.get() != null);
        assertEquals(Name.error, result.get().key);
        assertTrue(((Err) result.get().getValue()).exception instanceof ArithmeticException);


    }

    @Test
    public void testApplySingleLambda() {
        /*
                function mul(a) = a * 2
                function apply(func, a) = func(a)

                check(apply(mul, a))
         */

        // function mul(a) = a * 2


        Function<Integer> mul = BinOps.mul().constant(Name.rightArg, 2);
        mul.label("*");
        FunctionSignature<Integer> functionSignatureMul = functionSignature(mul);
        functionSignatureMul.propagate(Name.a, Name.leftArg, mul);
        functionSignatureMul.label("MUL");

        //function apply(func, a) = func(a)
        FunctionStub<Integer> stubFunc = FunctionStub.of(Name.func);
        stubFunc.label("funcStub");

        FunctionSignature<Integer> functionSignatureApply = functionSignature(stubFunc);
        functionSignatureApply.propagate(Name.a, Name.a, stubFunc);
        functionSignatureApply.propagate(Name.func, Name.func, stubFunc);
        functionSignatureApply.label("APPLY");

        // check(apply(mul, a))
        FunctionCall<Integer> functionCallApply = functionCall(functionSignatureApply);
        functionCallApply.label("functionCallApply");
        Int result = Int(0);
        Action resultListener = action((Consumer<Message> & Serializable) i -> result.setValue((Integer) i.getValue()));
        resultListener.param(Name.result);
        functionCallApply.returnTo(resultListener, Name.result);

        Origin origin = Origin.origin(resultListener);

        result.setValue(0);
        functionCallApply.tell(message(Name.func, functionSignatureMul, origin));
        functionCallApply.tell(message(Name.a, _3, origin));
        await(() -> result.value != 0);
        assertEquals(Integer.valueOf(6), result.value);

        result.setValue(0);
        functionCallApply.tell(message(Name.func, functionSignatureMul, origin));
        functionCallApply.tell(message(Name.a, _1, origin));
        await(() -> result.value != 0);
        assertEquals(Integer.valueOf(2), result.value);

    }

    @Test
    public void testConstLambda() {
        /*
                function mul(a) = a * 2

                function apply(func) = func(2)

                check(apply(mul, a))
         */

        //  function mul(a) = a * 2

        Function<Integer> mul = BinOps.mul().constant(Name.rightArg, 2);
        mul.label("*");
        FunctionSignature<Integer> functionSignatureMul = functionSignature(mul);
        functionSignatureMul.propagate(Name.a, Name.leftArg, mul);
        functionSignatureMul.label("MUL");

        //function apply(func, a) = func(a)
        FunctionStub<Integer> stubFunc = FunctionStub.of(Name.func);
        stubFunc.constant(Name.a, _2);
        stubFunc.label("stubFunc");

        FunctionSignature<Integer> functionSignatureApply = functionSignature(stubFunc);
        functionSignatureApply.label("APPLY");
        functionSignatureApply.propagate(Name.func, Name.func, stubFunc);

        // check(apply(mul, a))
        FunctionCall<Integer> functionCallApply = functionCall(functionSignatureApply);
        functionCallApply.label("apply");
        Int result = Int(0);
        Action resultListener = action((Consumer<Message> & Serializable) i -> result.setValue((Integer) i.getValue()));
        resultListener.param(Name.result);
        functionCallApply.returnTo(resultListener, Name.result);

        Origin origin = Origin.origin(resultListener);
        functionCallApply.tell(message(Name.func, functionSignatureMul, origin));

        await(() -> result.value != 0);
        assertEquals(_4, result.value);

    }


    @Test
    public void testConstLambdaNested() {
        /*
                function mul(a) = a * 2

                function apply(func) = func(func(2))

                check(apply(mul, a))
         */

        //  function mul(a) = a * 2
        Function<Integer> mul = BinOps.mul().constant(Name.rightArg, 2);
        mul.label("*");
        FunctionSignature<Integer> functionSignatureMul = functionSignature(mul);
        functionSignatureMul.propagate(Name.a, Name.leftArg, mul);
        functionSignatureMul.label("MUL");

        //function apply(func) = func(func(2))
        FunctionStub<Integer> stubFuncInner = FunctionStub.of(Name.func);
        stubFuncInner.constant(Name.a, _2);
        stubFuncInner.label("stubFuncInner");

        FunctionStub<Integer> stubFuncOuter = FunctionStub.of(Name.func);
        stubFuncOuter.label("stubFuncOuter");
        stubFuncInner.returnTo(stubFuncOuter, Name.a);

        FunctionSignature<Integer> functionSignatureApply = functionSignature(stubFuncOuter);
        functionSignatureApply.label("APPLY");
        functionSignatureApply.propagate(Name.func, Name.func, stubFuncOuter);
        functionSignatureApply.propagate(Name.func, Name.func, stubFuncInner);

        // check(apply(mul, a))
        FunctionCall<Integer> functionCallApply = functionCall(functionSignatureApply);
        functionCallApply.label("apply");
        Int result = Int(0);
        Action resultListener = action((Consumer<Message> & Serializable) i -> result.setValue((Integer) i.getValue()));
        resultListener.param(Name.result);
        functionCallApply.returnTo(resultListener, Name.result);

        Origin origin = Origin.origin(resultListener);
        functionCallApply.tell(message(Name.func, functionSignatureMul, origin));

        await(() -> result.value == 8);

    }

    @Test
    public void testPartialApplicationOnPrimitiveFunction() {
        /*

                function inc = a -> a + 1

                check(inc(0))
                check(inc(1))
         */

        Int result = Int(0);
        Action resultListener = action((Consumer<Message> & Serializable) i -> result.setValue((Integer) i.getValue()));
        resultListener.param(Name.result);

        BinOp<Integer, Integer, Integer> add = add();
        PartialFunctionApplication<Integer> partialAdd = partialApplication(add, list(Name.b));
        partialAdd.label("+X");
        partialAdd.propagate(Name.b, Name.rightArg, add);
        partialAdd.propagate(Name.a, Name.leftArg, add);

        FunctionCall<Integer> inc = functionCall(partialAdd);
        partialAdd.returnTo(inc, Name.result);
        inc.returnTo(resultListener, Name.result);
        inc.label("INC");

        //TODO test for nested use for the same partial function
        //in combination with other partial functions in the same execution
        //TODO right now in this case the state of PartiallyAppliedFunction won't get cleared up after return
        Origin origin = Origin.origin(resultListener, 0L);
        inc.tell(message(Name.b, _1, origin));
        assertResult(_1, _0, inc, result, origin);
        //assertResult(_2, _1, inc, result, origin);
        inc.tell(message(Name.removePartialAppValues, null, origin));

        origin = Origin.origin(resultListener, 1L);
        inc.tell(message(Name.b, _3, origin));
        assertResult(_3, _0, inc, result, origin);
        //assertResult(_4, _1, inc, result, origin);
        inc.tell(message(Name.removePartialAppValues, null, origin));

    }


    @Test
    public void testPartialApplicationOnLambda() {
        /*
                function inc = a -> a + 1
                function apply(func, a) = func(a)

                check(apply(inc, 0))
                check(apply(inc, 1))
         */

        Int result = Int(0);
        Action resultListener = action((Consumer<Message> & Serializable) i -> result.setValue((Integer) i.getValue()));
        resultListener.param(Name.result);
        Origin origin = Origin.origin(resultListener);

        BinOp<Integer, Integer, Integer> add = add();
        PartialFunctionApplication<Integer> partialAdd = partialApplication(add, list(Name.b));
        partialAdd.label("ADD(_,X)");
        partialAdd.propagate(Name.a, Name.leftArg, add);
        partialAdd.propagate(Name.b, Name.rightArg, add);

        // by way of using the apply function
        //function apply(func, a) = func(a)
        FunctionStub<Integer> stubFunc = FunctionStub.of(Name.func);
        stubFunc.label("stubFunc");

        // it is intended that the partialy applied function is created before passing it to apply
        // applying values and removing them again has to happen at the same function call level
        FunctionSignature<Integer> functionSignatureApply = functionSignature(stubFunc);
        functionSignatureApply.propagate(Name.a, Name.a, stubFunc);
        functionSignatureApply.propagate(Name.func, Name.func, stubFunc);

        // check(apply(mul, a))
        FunctionCall<Integer> functionCallApply = functionCall(functionSignatureApply);
        functionCallApply.label("functionCallApply");
        functionCallApply.returnTo(resultListener, Name.result);

        resultListener.propagate(Name.b, Name.b, partialAdd);
        resultListener.onReceivedReturnSend(Name.removePartialAppValues, null, partialAdd);
        resultListener.tell(message(Name.b, _1, origin));

        functionCallApply.tell(message(Name.func, partialAdd, origin));
        functionCallApply.tell(message(Name.a, _3, origin));

        result.setValue(0);
        await(() -> result.value != 0);
        assertEquals(Integer.valueOf(4), result.value);

    }

    private void assertResult(Integer expected, Integer a, FunctionCall<Integer> inc, Int result, Origin origin) {
        result.setValue(0);
        inc.tell(message(Name.a, a, origin));
        await(() -> result.value != 0);
        assertEquals(expected, result.value);
        logger.info("asserted " + expected);
    }

    @Test
    public void testQuicksort() {
        Function<ArrayList<Integer>> qsortCall = functionCall(getQuicksortSignature());
        qsortCall.label("qsortCall");

        checkQuicksort(emptyList, emptyList, qsortCall);
        checkQuicksort(list(2), list(2), qsortCall);
        checkQuicksort(list(1, 2), list(2, 1), qsortCall);
        checkQuicksort(list(1, 2, 3), list(2, 1, 3), qsortCall);
        checkQuicksort(list(0, 1, 2, 3, 4, 5), list(5, 4, 3, 2, 1, 0), qsortCall);
        checkQuicksort(list(0, 1, 2, 3, 4, 5), list(0, 1, 2, 3, 4, 5), qsortCall);

        ArrayList<Integer> randList = randomList(100);
        ArrayList<Integer> randListSorted = new ArrayList<>(randList);
        randListSorted.sort(Integer::compareTo);
        checkQuicksort(randListSorted, randList, qsortCall);
        System.out.println("done sorting " + randList.size());
    }

    @Test
    public void testParallelQuicksort() {

        Function<ArrayList<Integer>> qsortCall = functionCall(getQuicksortSignature());

        Gateway<ArrayList<Integer>> gateway = new Gateway<>();
        ConcurrentLinkedQueue<ArrayList<Integer>> resultCollector = new ConcurrentLinkedQueue<>();

        int parallelRuns = 5;
        int listSize = 15;
        for (int i = 0; i < parallelRuns; i++) {
            runQuicksortThread(i, qsortCall, gateway, randomList(listSize), resultCollector);
        }

        await(() -> resultCollector.size() == parallelRuns);
        resultCollector.forEach(l -> {
            ArrayList<Integer> sorted = new ArrayList<>(l);
            sorted.sort(Integer::compareTo);
            assertEquals(sorted, l);
        });
    }

    private void runQuicksortThread(int runId, Function<ArrayList<Integer>> sumCall, Gateway<ArrayList<Integer>> gateway, ArrayList<Integer> input, ConcurrentLinkedQueue<ArrayList<Integer>> resultCollector) {
        new Thread(() -> {
            System.out.println(runId + " sorting " + input);
            gateway.execute(sumCall, v -> {
                resultCollector.add(v);
                System.out.println(runId + " received " + v.toString());
            }).param(Name.list, input);
        }).start();

    }

    private long executions;

    private List<Integer> checkQuicksort(List<Integer> expected, ArrayList<Integer> input, Function<ArrayList<Integer>> qsortCall) {
        System.out.println("sorting a list of size " + input.size());
        ArrayList<Integer> result = new ArrayList<>();
        Int lastInt = Int(-1);
        Action resultListener = action((Consumer<Message> & Serializable) i -> {
            result.addAll((List<Integer>) i.getValue());
            lastInt.value = 1;
        }).param(Name.result);
        resultListener.propagate(Name.list, Name.list, qsortCall);

        qsortCall.returnTo(resultListener, Name.result);

        Origin origin = origin(nop, new ComputationPath(executions++), "", "0");
        resultListener.tell(message(Name.list, input, origin));

        await(() -> expected.size() > 0 ? result.size() == expected.size() : lastInt.value > 0);
        result.sort(Integer::compareTo);
        assertEquals(expected, result);
        return result;
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
    public void testFilter() {
        /*
            input = List(1,2,3,4,5,6,7)
            check(filter(input, a -> iff (a == null) {false} else {a mod 2 == 0}))
            */

        Function<ArrayList<Integer>> filterCall = functionCall(filterSignature().get());
        filterCall.label("filter");
        FunctionSignature<Boolean> predicate = getModEqZero();
        predicate.constant(Name.m, 2);
        checkFilter(emptyList, emptyList, filterCall, predicate);
        checkFilter(list(2), list(2), filterCall, predicate);
        checkFilter(list(2, 2), list(2, 2), filterCall, predicate);
        checkFilter(emptyList, list(1), filterCall, predicate);
        checkFilter(emptyList, list(1, 1), filterCall, predicate);
        checkFilter(list(2), list(1, 2, 3), filterCall, predicate);
        checkFilter(list(0, 2, 4, 6, 8), list(6, 7, 8, 2, 3, 4, 5, 9, 1, 0), filterCall, predicate);
    }

    private ArrayList<Integer> list(Integer... i) {
        return new ArrayList<>(asList(i));
    }

    private int filterExecutions = 0;
    private void checkFilter(ArrayList<Integer> expected, ArrayList<Integer> input, Function<ArrayList<Integer>> filterCall, FunctionSignature<Boolean> predicate) {
        System.out.println("Checking filter for list of size " + input.size());
        ArrayList<Integer> result = new ArrayList<>();
        Int lastInt = Int(-1);
        Action resultListener = action((Consumer<Message> & Serializable) i -> {
            result.addAll((List<Integer>) i.getValue());
            lastInt.value = 1;
        }).param(Name.result);
        resultListener.propagate(Name.list, Name.list, filterCall);
        resultListener.propagate(Name.predicate, Name.predicate, filterCall);
        filterCall.returnTo(resultListener, Name.result);


        Origin origin = new Origin(nop, new ComputationPath(++filterExecutions), "","") ;
        resultListener.tell(message(Name.list, input, origin));
        resultListener.tell(message(Name.predicate, predicate, origin));

        await(() -> expected.size() > 0 ? result.size() == expected.size() : lastInt.value > 0);
        result.sort(Integer::compareTo);
        assertEquals(expected, result);
    }


    private List<String> list(String... p) {
        return asList(p);
    }

    private FunctionSignature<Boolean> getModEqZero() {
        // (a,m) -> (a mod m) == 0

        Function<Boolean> eqZero = BinOps.eq().constant(Name.rightArg, _0);
        Function<Integer> mod = BinOps.mod().returnTo(eqZero, Name.leftArg);

        FunctionSignature<Boolean> signature = functionSignature(eqZero);
        signature.label("MODm==0");
        signature.propagate(Name.m, Name.rightArg, mod);
        signature.propagate(Name.arg, Name.leftArg, mod);

        return signature;
    }

    @Test
    public void testgetModEqZero() {

        FunctionSignature<Boolean> f = getModEqZero();

        FunctionCall<Boolean> c = functionCall(f);
        Stream.of(0, 1, 2, 3, 4, 5, 6).forEach(i ->
                assertEquals((i % 2) == 0, sync(c)
                        .param(Name.arg, i)
                        .param(Name.m, 2)
                        .call()));
    }

    @Test
    public void testGateway() {
        Gateway<Integer> gateway = intGateway();
        BinOp<Integer, Integer, Integer> mul = BinOps.mul();

        Execution<Integer> ex = gateway
                .execute(mul)
                .param(Name.leftArg, 3)
                .param(Name.rightArg, 2);
        assertEquals(_6, ex.get());


        Int result = Int(0);
        ex = gateway.execute(BinOps.mul(), result::setValue)
                .param(Name.leftArg, 2)
                .param(Name.rightArg, 2);
        await(() -> result.value.equals(_4));
        assertEquals(_4, ex.get());


        List<Integer> results = Stream.of(0, 1, 2, 3, 4, 5)
                .parallel()
                .map(i -> gateway
                        .execute(mul)
                        .param(Name.leftArg, i)
                        .param(Name.rightArg, i).get()

                ).sorted(Integer::compareTo)
                .collect(Collectors.toList());
        assertEquals(asList(0, 1, 4, 9, 16, 25), results);

        ConcurrentLinkedQueue<Integer> resultCollector = new ConcurrentLinkedQueue<>();
        Stream.of(0, 1, 2, 3, 4, 5)
                .parallel()
                .forEach(i -> gateway
                        .execute(mul, resultCollector::add)
                        .param(Name.leftArg, i)
                        .param(Name.rightArg, i)
                );
        await(() -> resultCollector.size() == 6);
        assertEquals(asList(0, 1, 4, 9, 16, 25),
                resultCollector.stream()
                        .sorted(Integer::compareTo)
                        .collect(Collectors.toList()));
    }


    @Test
    public void testCallSync() {

        /*
            sync(a + 1); // a = 2

         */

        Function<Integer> add = add();

        assertEquals(_1, sync(add)
                .param(Name.leftArg, 0)
                .param(Name.rightArg, 1)
                .call());

        add.constant(Name.rightArg, 3);
        assertEquals(_5, sync(add)
                .param(Name.leftArg, 2)
                .call());

    }


    @Test
    public void testCallSyncEmptyParams() {

        /*

            func f = 1; // or so

            sync(f);

         */

        assertEquals(_1, sync(constant(1)).call());
    }

    @Test
    public void testInitializeAndTerminateComputation_ConstValuesOnly() {

        /*
            print(1 + 2)
         */

        Int result = Int(0);
        Action resultListener = action((Consumer<Message> & Serializable) i -> result.setValue((Integer) i.getValue()));

        resultListener.param(Name.result);
        Function<Integer> add = add()
                .returnTo(resultListener, Name.result)
                .constant(Name.leftArg, 1)
                .constant(Name.rightArg, 2);

        resultListener.kickOff(add);
        assertTrue(resultListener.executionStates.isEmpty());
        assertTrue(add.executionStates.isEmpty());

        Origin origin = Origin.origin(resultListener);
        resultListener.tell(message(Name.kickOff, null, origin));

        await(() -> result.value != 0);
        assertEquals(_3, result.value);

        assertEquals(0, resultListener.executionStates.size());
        assertEquals(0, add.executionStates.size());

    }

    @Test
    public void testLet() {

        /*  let has no constructs on its own, it just returns assigned values as messages to the surrounding function
            which propagates them

        func f(){
            let a = 1;
            let b = a + 2;
            let c = b * b;
            return c;
          }

        check(f())

        */

        Int result = Int(0);
        Action resultListener = action((Consumer<Message> & Serializable) i -> result.setValue((Integer) i.getValue()));

        Function<Integer> mul = BinOps.mul();
        Function<Integer> functionSignature = functionSignature(mul);

        Function<Integer> constOne = new Const(_1).returnTo(functionSignature, Name.a);

        Function<Integer> add = add()
                .returnTo(functionSignature, Name.b)
                .constant(Name.rightArg, _2);

        ((FunctionSignature<Integer>) functionSignature)
                .letKeys(Name.a, Name.b)
                .kickOff(constOne) // const must be dependent too but needs no propagations
                .propagate(Name.a, Name.leftArg, add)
                .propagate(Name.b, Name.leftArg, mul)
                .propagate(Name.b, Name.rightArg, mul);

        Function<Integer> functionCall = functionCall(functionSignature)
                .returnTo(resultListener, Name.result);
        resultListener.kickOff(functionCall);
        resultListener.param(Name.result);

        resultListener.tell(message(Name.kickOff, null, Origin.origin(resultListener)));

        await(() -> result.value != 0);
        assertEquals(Integer.valueOf(9), result.value);

    }

    @Test
    public void testFunctionCallNoParams() {

        /*

            function get() = 1;

            print(get())

         */
        Int result = Int(0);
        Action resultMonitor = action((Consumer<Message> & Serializable) i -> result.setValue((int) i.getValue()));

        Const constOne = constant(1);
        Function<Integer> callee = functionSignature(constOne);
        Function<Integer> caller = functionCall(callee);
        caller.returnTo(resultMonitor, Name.result);
        resultMonitor.kickOff(caller);

        caller.kickOff(callee);
        callee.kickOff(constOne);

        resultMonitor.param(Name.result);

        resultMonitor.tell(message(Name.kickOff, null, Origin.origin(resultMonitor)));
        await(() -> result.value != 0);

        assertEquals(_1, result.value);

    }

    @Test
    public void testFunctionCall() {

        /*

            function add(a, b) = (a + b);

            check(add(1, 2)) // run in ...
            check(add(3, 4)) // ... parallel

         */
        Ensemble.instance().setRunProperties(DEBUG);

        Map<String, Integer> results = new HashMap<>();

        Function<Integer> add = add();

        Function<Integer> functionAdd = functionSignature(add);
        functionAdd.label("ADD");
        functionAdd.propagate(Name.a, Name.leftArg, add);
        functionAdd.propagate(Name.b, Name.rightArg, add);

        Action resultMonitor = action((Consumer<Message> & Serializable) i -> results.put(i.key, (int) i.getValue()));

        Function<Integer> callerA = functionCall(functionAdd).returnTo(resultMonitor, "resultA");
        Function<Integer> callerB = functionCall(functionAdd).returnTo(resultMonitor, "resultB");

        resultMonitor.param(callerA.returnKey);
        resultMonitor.param(callerB.returnKey);

        Origin originA = Origin.origin(nop);
        callerA.tell(message(Name.a, 1, originA));
        callerA.tell(message(Name.b, 2, originA));

        Origin originB = Origin.origin(nop);
        callerB.tell(message(Name.a, 3, originB));
        callerB.tell(message(Name.b, 4, originB));

        await(() -> results.size() == 2);
        assertEquals(results.get(callerA.returnKey), _3);
        assertEquals(results.get(callerB.returnKey), Integer.valueOf(7));
        results.put("", 0);
    }

    @Test
    public void testSerialFunctionCall() {

        /*

            function inc(a) = a + 1;

            check(inc(a) * inc(a))

         */

        Int result = Int(0);

        Function<Integer> inc = add().constant(Name.rightArg, _1);
        Function<Integer> signatureInc = functionSignature(inc);
        signatureInc.propagate(Name.a, Name.leftArg, inc);

        Function<Integer> mul = BinOps.mul();
        Function<Integer> callerL = functionCall(signatureInc).returnTo(mul, Name.leftArg);
        Function<Integer> callerR = functionCall(signatureInc).returnTo(mul, Name.rightArg);

        signatureInc.label("INC");
        callerL.label("callerL");
        callerR.label("callerR");

        Action resultMonitor = action((Consumer<Message> & Serializable) i -> result.setValue((int) i.getValue())).param(Name.result);
        resultMonitor.propagate(Name.a, Name.a, mul);
        mul.returnTo(resultMonitor, Name.result);
        mul.propagate(Name.a, Name.a, callerL);
        mul.propagate(Name.a, Name.a, callerR);

        Origin origin = originForExecutionId(resultMonitor, 0L);
        resultMonitor.tell(message(Name.a, _1, origin));

        await(() -> result.value == 4);

    }

    @Ignore
    @Test
    public void testNestedFunctionCall() {

        /*

            function pow2(a) = a * a;

            check(pow2(pow2(a)))

         */

        Int result = Int(0);
        Action resultMonitor = action((Consumer<Message> & Serializable) i -> result.setValue((int) i.getValue()));
        resultMonitor.param(Name.result);

        Function<Integer> mul = BinOps.mul();
        Function<Integer> mulSignature = functionSignature(mul);
        // each nested function call needs its separate key
        //TODO not applicable any more with parameterLists. Signature will wait for a AND a1, but in each call
        //only one of them will arrive
        mulSignature.propagate(Name.a, Name.leftArg, mul);
        mulSignature.propagate(Name.a, Name.rightArg, mul);
        mulSignature.propagate(Name.a1, Name.leftArg, mul);
        mulSignature.propagate(Name.a1, Name.rightArg, mul);

        Function<Integer> innerCaller = functionCall(mulSignature).constant(Name.a, 2);
        Function<Integer> outerCaller = functionCall(mulSignature);
        outerCaller.propagate(Name.a, Name.a, innerCaller);
        innerCaller.returnTo(outerCaller, Name.a1);
        outerCaller.returnTo(resultMonitor, Name.result);

        mul.label("mul");
        mulSignature.label("MUL");
        resultMonitor.label("gateway");
        outerCaller.label("outerCaller");
        innerCaller.label("innerCaller");

        Origin origin = originForExecutionId(resultMonitor, 0L);
        innerCaller.tell(message(Name.a, _2, origin));

        await(() -> result.value == 16);

    }


    @Test
    public void testIf() {

        /*
             for cases      a   b
                            4   1
                            1   4

         * output = if (a > b)
         *              a - b
         *          else
         *             b - a
         */

        Function<Integer> _if = If.createIf();

        Int result = Int(0);
        Action resultMonitor = action((Consumer<Message> & Serializable) i -> result.setValue((int) i.getValue()));
        resultMonitor.param(Name.result);

        Function<Boolean> gt = BinOps.gt().returnTo(_if, Name.condition);
        Function subT = BinOps.sub().returnTo(_if, Name.onTrue);
        Function subF = BinOps.sub().returnTo(_if, Name.onFalse);
        subT.label("subT");
        subF.label("subF");

        _if.returnTo(resultMonitor, Name.result);
        _if.propagate(Name.b, Name.leftArg, subF);
        _if.propagate(Name.a, Name.rightArg, subF);

        _if.propagate(Name.a, Name.leftArg, gt);
        _if.propagate(Name.a, Name.leftArg, subT);
        _if.propagate(Name.b, Name.rightArg, gt);
        _if.propagate(Name.b, Name.rightArg, subT);

        resultMonitor.propagate(Name.a, Name.a, _if);
        resultMonitor.propagate(Name.b, Name.b, _if);

//  kickOff must only be used where no params get passed in at all. it proved bad otherwise, since kickOff may
//  arrive after the params and after a result has already been returned, resulting in a new executionState being
//  created that never again will be removed
//        resultMonitor.kickOff(_if);
//        _if.kickOff(gt);
//        _if.kickOff(subT);
//        _if.kickOff(subF);

        // True-path
        Origin originA = originForExecutionId(resultMonitor, 0L);
        resultMonitor.tell(message(Name.a, _4, originA));
        resultMonitor.tell(message(Name.b, _1, originA));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);

        // False-path
        result.setValue(0);
        Origin originB = originForExecutionId(resultMonitor, 1L);
        resultMonitor.tell(message(Name.a, _1, originB));
        resultMonitor.tell(message(Name.b, _4, originB));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        result.setValue(0);
    }

    @Test
    public void testSequentialIf() {

        /* sequential if requires specific control of computationBranch initializations and propagations
           depending on the calculated value of the condition

             for cases      a   b
                            4   1
                            1   4

         * output = if (a > b)
         *              a - b
         *          else
         *             b - a
         */

        Iff<Integer> iff = iff();

        Int result = Int(0);
        Action resultMonitor = action((Consumer<Message> & Serializable) i -> result.setValue((int) i.getValue()));
        resultMonitor.param(Name.result);

        Function<Boolean> gt = BinOps.gt()
                .returnTo(iff, Name.condition);
        Function subT = BinOps.sub()
                .returnTo(iff, Name.onTrue);
        Function subF = BinOps.sub()
                .returnTo(iff, Name.onFalse);

        iff.returnTo(resultMonitor, Name.result);
        // condition may propagate, init and finalize as usual
        iff.propagate(Name.a, Name.leftArg, gt);
        iff.propagate(Name.b, Name.rightArg, gt);

        // true and false branches must propagate, init and finalize differently, and also separately from the condition
        iff.propagateOnTrue(Name.a, Name.leftArg, subT);
        iff.propagateOnTrue(Name.b, Name.rightArg, subT);


        iff.propagateOnFalse(Name.b, Name.leftArg, subF);
        iff.propagateOnFalse(Name.a, Name.rightArg, subF);

        resultMonitor.propagate(Name.a, Name.a, iff);
        resultMonitor.propagate(Name.b, Name.b, iff);

        // True-path
        Origin computationA = originForExecutionId(resultMonitor, 0L);
        resultMonitor.tell(message(Name.a, _4, computationA));
        resultMonitor.tell(message(Name.b, _1, computationA));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);

        // False-path
        result.setValue(0);
        Origin computationB = originForExecutionId(resultMonitor, 1L);
        resultMonitor.tell(message(Name.a, _1, computationB));
        resultMonitor.tell(message(Name.b, _4, computationB));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        result.setValue(0);

    }

    @Test
    public void testSIfWithPresetParameters() {
        /*
            // in this case propagation of "a" goes to ">" but also immediately to onTrue and onFalse params of if

         * output = if (a > b)
         *              a
         *          else
         *              b
         */

        Function<Integer> _if = iff();

        Int result = Int(0);
        Action resultMonitor = action((Consumer<Message> & Serializable) i -> result.setValue((int) i.getValue()));
        resultMonitor.param(Name.result);

        Function<Boolean> gt = BinOps.gt();

        gt.returnTo(_if, Name.condition);

        _if.returnTo(resultMonitor, Name.result);

        // an example how init/finalize could deviate from propagations, which reduces communication,
        // but may be confusing

        resultMonitor.propagate(Name.a, Name.leftArg, gt);
        resultMonitor.propagate(Name.b, Name.rightArg, gt);
        resultMonitor.propagate(Name.a, Name.onTrue, _if);
        resultMonitor.propagate(Name.b, Name.onFalse, _if);

        // True-path
        Origin originA = originForExecutionId(resultMonitor, 0L);
        resultMonitor.tell(message(Name.a, _4, originA));
        resultMonitor.tell(message(Name.b, _1, originA));
        await(() -> result.value != 0);
        assertEquals(_4, result.value);

        // False-path
        result.setValue(0);
        Origin originB = originForExecutionId(resultMonitor, 1L);
        resultMonitor.tell(message(Name.a, _1, originB));
        resultMonitor.tell(message(Name.b, _4, originB));
        await(() -> result.value != 0);
        assertEquals(_4, result.value);
        result.setValue(0);

    }

    private static int runId = 0;

    @Test
    public void testRecursion() {
        Int result = Int(0);
        Action resultMonitor = action((Consumer<Message> & Serializable) i -> result.setValue((int) i.getValue()));
        resultMonitor.param(Name.result);

        Function sumCall = getRecursiveSumCall();
        resultMonitor.propagate(Name.a, Name.a, sumCall);
        sumCall.returnTo(resultMonitor, Name.result);


        checksum(result, resultMonitor, runId++, 0, 0);
        checksum(result, resultMonitor, runId++, 1, 1);
        checksum(result, resultMonitor, runId++, 2, 3);
        checksum(result, resultMonitor, runId++, 10, 11 * 5);
        checksum(result, resultMonitor, runId++, 100, 101 * 50);
        //      checksum(result, resultMonitor, runId++, 20000, 20001 * 10000);
        //      checksum(result, resultMonitor, runId, 64000, 64001 * 32000); // close to MaxInt, isn't overflow-save

        testParallelRecursion(sumCall);
    }

    private Function<Integer> getRecursiveSumCall() {

               /* function sum(a) = if (a = 0 )
                                        0
                                   else
                                        a + sum(a - 1);
                    echo(sum(3));
         */

        Iff<Integer> _if = iff();
        _if.constant(Name.onTrue, _0);

        Function<Boolean> eq = BinOps.eq().constant(Name.rightArg, _0);
        Function<Integer> sub = BinOps.sub().constant(Name.rightArg, _1);
        Function<Integer> add = add();
        Function<Integer> sumSignature = functionSignature(_if);
        Function<Integer> sumReCall = functionCall(sumSignature);
        Function<Integer> sumCall = functionCall(sumSignature);

        sumSignature.propagate(Name.a, Name.a, _if);
        _if.propagate(Name.a, Name.leftArg, eq);
        _if.propagateOnFalse(Name.a, Name.leftArg, add);
        _if.propagateOnFalse(Name.a, Name.a, add);
        // can't propagate "a" to sumCallRec directly, so that it passes "a" on to sub ("a - 1") alone. it doesen't.
        // instead it causes another recursion with "a" alone rather than "a - 1" from sub (and maybe more mess, because
        // sum(a) will also tell a second "a" from the "a - 1", but that doesen't matter any more then)
        // one would have to distinguish somehow by key the "a" as the parameter in sum(a) from the a in "a - 1"
        // -> so this doesen't work:
        // add.propagate(Name.a, Name.a, sumCallRec);
        add.propagate(Name.a, Name.leftArg, sub);

        //TODO: the returning structure is symmetrical to the dependency structure, maybe setting up both can be unified
        eq.returnTo(_if, Name.condition);
        sub.returnTo(sumReCall, Name.a);
        sumReCall.returnTo(add, Name.rightArg);
        add.returnTo(_if, Name.onFalse);

        return sumCall;
    }

    private void testParallelRecursion(Function<Integer> sumCall) {
        Gateway<Integer> gateway = intGateway();

        ConcurrentLinkedQueue<Integer> input = new ConcurrentLinkedQueue<>();
        Stream.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6)
                .forEach(input::add);

        ConcurrentLinkedQueue<Integer> resultCollector = new ConcurrentLinkedQueue<>();

        Thread t0 = createThread(0, sumCall, gateway, input, resultCollector);
        Thread t1 = createThread(1, sumCall, gateway, input, resultCollector);
        Thread t2 = createThread(2, sumCall, gateway, input, resultCollector);

        t0.start();
        t1.start();
        t2.start();

        await(() -> resultCollector.size() == 54);
        assertEquals(asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 6, 6, 6, 6, 6, 6, 10, 10, 10, 10, 10, 10, 15, 15, 15, 15, 15, 15, 21, 21, 21, 21, 21, 21),
                resultCollector.stream()
                        .sorted(Integer::compareTo)
                        .collect(Collectors.toList()));
    }

    private Thread createThread(int i, Function<Integer> sumCall, Gateway<Integer> gateway, ConcurrentLinkedQueue<Integer> input, ConcurrentLinkedQueue<Integer> resultCollector) {
        return new Thread(() -> {

            Integer next = input.poll();
            while (next != null) {
                gateway
                        .execute(sumCall, v -> {
                            resultCollector.add(v);
                            System.out.println("Thread " + i + " received recursive sum: " + v);

                        })
                        .param(Name.a, next);
                next = input.poll();
            }
        });
    }

    private void checksum(Int result, Action resultMonitor, int executionId, int sumOf, Integer expected) {
        Long start = System.nanoTime();

        result.value = -1;

        Origin run = originForExecutionId(resultMonitor, (long) executionId);
        resultMonitor.tell(message(Name.a, sumOf, run));

        await(() -> result.value >= 0);
        assertEquals(expected, result.value);

        Long stop = System.nanoTime();
        long elapsed = (stop - start) / 1000000;

        System.out.println(String.format("sum for %d took %d millis (%d)", sumOf, elapsed, result.value));

    }

    private Integer sum(Integer i) {
        if (i == 0) {
            return 0;
        }
        return i + sum(i - 1);
    }

    @Test
    public void recursiveSumInJava() {
        Long start = System.nanoTime();
        Integer max = 7000;
        Integer sum = sum(max);
        long elapsed = (System.nanoTime() - start) / 1000000;

        System.out.println(String.format("(java function: sum of %d (%d) in %d millis", max, sum, elapsed));
    }


    private Origin originForExecutionId(Action a, Long executionId) {
        return origin(a, new ComputationPath(executionId), "", "0");
    }

    private List<Integer> sorted(List<Integer> list) {
        ArrayList<Integer> result = new ArrayList<>(list);
        result.sort(Integer::compareTo);
        return result;
    }

    @Ignore
    @Test
    public void testRecoveryRecursion() {
        Ensemble.instance().setRunProperties(PERSIST);
        Int result = Int(0);
        Action resultMonitor = action((Consumer<Message> & Serializable) i -> result.setValue((int) i.getValue()));
        Function<Integer> sumCall = getRecursiveSumCall();
        resultMonitor.propagate(Name.a, Name.a, sumCall);
        sumCall.returnTo(resultMonitor, Name.result);

        Origin run = originForExecutionId(resultMonitor, (long) ++runId);
        resultMonitor.tell(message(Name.a, 10, run));


        await(175);
        System.out.println("terminating before resume");
        Ensemble.terminate();
        Ensemble.reset();
        Ensemble.instance().setRunProperties(PERSIST);
        await(5000);
        System.out.println("resuming");

        result.setValue(0);
        resultMonitor = action((Consumer<Message> & Serializable) i -> result.setValue((int) i.getValue()));
        sumCall = getRecursiveSumCall();
        resultMonitor.propagate(Name.a, Name.a, sumCall);
        sumCall.returnTo(resultMonitor, Name.result);


        await(() -> result.value > 0);
        System.out.println("recovered sum was: " + result.value);
        Integer expected = 11 * 5;
        assertEquals(expected, result.value);
    }

    @Test
    public void testRecoveryQuicksort() {
        if (1 == 1) {
            throw new IllegalStateException();
        }
        Ensemble.instance().setRunProperties(PERSIST);

        ArrayList<Integer> input = randomList(200);
        List<Integer> result = new ArrayList<>();

        Origin origin = origin(nop, new ComputationPath(executions++), "", "0");
        setUpResultListener(functionCall(getQuicksortSignature()), result)
                .tell(message(Name.list, input, origin));

        await(3000);
        System.out.println("terminating before resume");
        Ensemble.terminate();
        Ensemble.reset();
        Ensemble.instance().setRunProperties(PERSIST, SHOW_STACKS);
        await(3000);
        System.out.println("resuming");

        // only recreate actors. they should replay all messages until now and resume the computation
        setUpResultListener(functionCall(getQuicksortSignature()), result);

        await(() -> result.size() == input.size());
        assertEquals(sorted(input), result);
        System.out.println(result);/**/
    }

    @Test
    public void testRecoveryPartialApplicationOnLambda() {
        /*
                function inc = a -> a + 1
                function apply(func, a) = func(a)

                check(apply(inc, 0))
                check(apply(inc, 1))
         */

        if (1 == 1) {
            throw new IllegalStateException();
        }
        Int result = Int(0);
        Action resultListener = action((Consumer<Message> & Serializable) i -> result.setValue((Integer) i.getValue()));
        resultListener.param(Name.result);
        Origin origin = Origin.origin(resultListener);

        PartialAppOnLambda partialAppOnLambda = new PartialAppOnLambda(resultListener).create();
        FunctionCall<Integer> functionCallApply = partialAppOnLambda.getFunctionCallApply();

        resultListener.tell(message(Name.b, _1, origin));
        functionCallApply.tell(message(Name.func, partialAppOnLambda.getPartialAdd(), origin));
        functionCallApply.tell(message(Name.a, _3, origin));

        result.setValue(0);
        await(() -> result.value != 0);
        assertEquals(Integer.valueOf(4), result.value);

        await(10000);
        System.out.println("terminating before resume");
        Ensemble.terminate();
        Ensemble.reset();
        Ensemble.instance().setRunProperties(PERSIST);
        await(1000);
        System.out.println("resuming");

        result.setValue(0);
        resultListener = action((Consumer<Message> & Serializable) i -> result.setValue((Integer) i.getValue()));
        resultListener.param(Name.result);
        new PartialAppOnLambda(resultListener).create();
        await(() -> result.value != 0);
        assertEquals(Integer.valueOf(4), result.value);
    }

    private Action setUpResultListener(Function<ArrayList<Integer>> qsortCall, List<Integer> result) {
        Action resultListener = action((Consumer<Message> & Serializable) i -> {
            result.addAll((List<Integer>) i.getValue());
        }).param(Name.result);
        resultListener.propagate(Name.list, Name.list, qsortCall);
        qsortCall.returnTo(resultListener, Name.result);
        return resultListener;
    }

    private class PartialAppOnLambda {
        private Action resultListener;
        private PartialFunctionApplication<Integer> partialAdd;
        private FunctionCall<Integer> functionCallApply;

        PartialAppOnLambda(Action resultListener) {
            this.resultListener = resultListener;
        }

        PartialFunctionApplication<Integer> getPartialAdd() {
            return partialAdd;
        }

        FunctionCall<Integer> getFunctionCallApply() {
            return functionCallApply;
        }

        PartialAppOnLambda create() {
            BinOp<Integer, Integer, Integer> add = add();
            partialAdd = partialApplication(add, list(Name.b));
            partialAdd.label("ADD(_,X)");
            partialAdd.propagate(Name.a, Name.leftArg, add);
            partialAdd.propagate(Name.b, Name.rightArg, add);

            // by way of using the apply function
            //function apply(func, a) = func(a)
            FunctionStub<Integer> stubFunc = FunctionStub.of(Name.func);
            stubFunc.label("stubFunc");

            // it is intended that the partialy applied function is created before passing it to apply
            // applying values and removing them again has to happen at the same function call level
            FunctionSignature<Integer> functionSignatureApply = functionSignature(stubFunc);
            functionSignatureApply.propagate(Name.a, Name.a, stubFunc);
            functionSignatureApply.propagate(Name.func, Name.func, stubFunc);

            // check(apply(mul, a))
            functionCallApply = functionCall(functionSignatureApply);
            functionCallApply.label("functionCallApply");
            functionCallApply.returnTo(resultListener, Name.result);

            resultListener.propagate(Name.b, Name.b, partialAdd);
            resultListener.onReceivedReturnSend(Name.removePartialAppValues, null, partialAdd);
            return this;
        }
    }
}