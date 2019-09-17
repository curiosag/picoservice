package miso;

import miso.ingredients.*;
import miso.ingredients.gateway.Execution;
import miso.ingredients.gateway.Gateway;
import miso.ingredients.nativeImpl.BinOps;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static miso.Int.Int;
import static miso.implementations.Filter.filterSignature;
import static miso.implementations.Quicksort.getQuicksortSignature;
import static miso.ingredients.Action.action;
import static miso.ingredients.Actresses.await;
import static miso.ingredients.CallSync.sync;
import static miso.ingredients.Const.constant;
import static miso.ingredients.FunctionCall.functionCall;
import static miso.ingredients.FunctionSignature.functionSignature;
import static miso.ingredients.Iff.iff;
import static miso.ingredients.Message.message;
import static miso.ingredients.Nop.nop;
import static miso.ingredients.Origin.origin;
import static miso.ingredients.PartialFunctionApplication.partialApplication;
import static miso.ingredients.gateway.Gateway.intGateway;
import static miso.ingredients.nativeImpl.BinOps.add;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServiceTest {
    private static final Integer _6 = 6;
    private static final Integer _5 = 5;
    private static final Integer _4 = 4;
    private static final Integer _3 = 3;
    private static final Integer _2 = 2;
    private static final Integer _1 = 1;
    private static final Integer _0 = 0;


    @After
    public void after() {
        Actresses.shutdown();
        Actresses.reset();
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
        FunctionSignature<Integer> functionSignatureMul = functionSignature(mul);
        functionSignatureMul.propagate(Name.a, Name.leftArg, mul);

        //function double(func, a) = func(a) + func(a)
        Function<Integer> add = add();

        FunctionSignature<Integer> functionSignatureDouble = functionSignature(add);
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
        Action resultListener = action(i -> result.setValue((Integer) i.value));
        resultListener.param(Name.result);
        functionCallDouble.returnTo(resultListener, Name.result);

        functionSignatureMul.label("MUL");
        functionSignatureDouble.label("DOUBLE");
        functionCallDouble.label("double");
        stubFuncLeft.label("stubLeft");
        stubFuncRight.label("stubRight");

        Origin origin = createSource(resultListener);
        functionCallDouble.receive(message(Name.func, functionSignatureMul, origin));
        functionCallDouble.receive(message(Name.a, _3, origin));
        await(() -> result.value != 0);
        assertEquals(Integer.valueOf(12), result.value);

    }

    @Test
    public void testSingleLambda() {
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
        stubFunc.label("stubFunc");

        FunctionSignature<Integer> functionSignatureApply = functionSignature(stubFunc);
        functionSignatureApply.propagate(Name.a, Name.a, stubFunc);
        functionSignatureApply.propagate(Name.func, Name.func, stubFunc);

        // check(apply(mul, a))
        FunctionCall<Integer> functionCallApply = functionCall(functionSignatureApply);
        functionCallApply.label("functionCallApply");
        Int result = Int(0);
        Action resultListener = action(i -> result.setValue((Integer) i.value));
        resultListener.param(Name.result);
        functionCallApply.returnTo(resultListener, Name.result);

        Origin origin = createSource(resultListener);
        functionCallApply.receive(message(Name.func, functionSignatureMul, origin));
        functionCallApply.receive(message(Name.a, _3, origin));
        await(() -> result.value != 0);
        assertEquals(Integer.valueOf(6), result.value);

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
        Action resultListener = action(i -> result.setValue((Integer) i.value));
        resultListener.param(Name.result);
        functionCallApply.returnTo(resultListener, Name.result);

        Origin origin = createSource(resultListener);
        functionCallApply.receive(message(Name.func, functionSignatureMul, origin));

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
        Action resultListener = action(i -> result.setValue((Integer) i.value));
        resultListener.param(Name.result);
        functionCallApply.returnTo(resultListener, Name.result);

        Origin origin = createSource(resultListener);
        functionCallApply.receive(message(Name.func, functionSignatureMul, origin));

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
        Action resultListener = action(i -> result.setValue((Integer) i.value));
        resultListener.param(Name.result);


        Origin origin = createSource(resultListener);
        BinOp<Integer, Integer, Integer> add = add();
        PartialFunctionApplication<Integer> partialAdd = partialApplication(add, list(Name.b));
        partialAdd.label("+X");
        partialAdd.propagate(Name.b, Name.rightArg, add);
        partialAdd.propagate(Name.a, Name.leftArg, add);

        FunctionCall<Integer> inc = functionCall(partialAdd);
        inc.returnTo(resultListener, Name.result);
        inc.label("INC");

        partialAdd.receive(message(Name.b, _1, origin));
        assertResult(_1, _0, inc, result, origin);
        assertResult(_2, _1, inc, result, origin);
        partialAdd.receive(message(Name.popPartialAppValues, null, origin));

        partialAdd.receive(message(Name.b, _2, origin));
        assertResult(_2, _0, inc, result, origin);
        assertResult(_3, _1, inc, result, origin);
        partialAdd.popPartialAppValues(origin);

    }

    @Test
    public void testPartialApplicationOnLambda() {
        /*
                function inc = a -> a + 1
                function apply(func, a) = func(a)

                check(apply(inc, 0))
                check(apply(inc, 1))
         */
        Actresses.debug();
        Actresses.trace();

        Int result = Int(0);
        Action resultListener = action(i -> result.setValue((Integer) i.value));
        resultListener.param(Name.result);
        Origin origin = createSource(resultListener);

        BinOp<Integer, Integer, Integer> add = add();
        PartialFunctionApplication<Integer> partialAdd = partialApplication(add, list(Name.b));
        partialAdd.label("ADD(_,X)");
        partialAdd.propagate(Name.a, Name.leftArg, add);
        partialAdd.propagate(Name.b, Name.rightArg, add);

        FunctionCall<Integer> inc = functionCall(partialAdd);
        inc.returnTo(resultListener, Name.result);
        inc.label("INC");

        // by way of direct call of the partially applied function
        inc.receive(message(Name.b, _1, origin));
        assertResult(_1, _0, inc, result, origin);
        assertResult(_2, _1, inc, result, origin);
        partialAdd.popPartialAppValues(origin);

        // by way of using the apply function
        //function apply(func, a) = func(a)
        FunctionStub<Integer> stubFunc = FunctionStub.of(Name.func);
        stubFunc.label("stubFunc");

        FunctionSignature<Integer> functionSignatureApply = functionSignature(stubFunc);
        functionSignatureApply.propagate(Name.a, Name.a, stubFunc);
        functionSignatureApply.propagate(Name.b, Name.b, stubFunc);
        functionSignatureApply.propagate(Name.func, Name.func, stubFunc);

        // check(apply(mul, a))
        FunctionCall<Integer> functionCallApply = functionCall(functionSignatureApply);
        functionCallApply.label("functionCallApply");
        functionCallApply.returnTo(resultListener, Name.result);

        functionCallApply.receive(message(Name.b, _1, origin));
        functionCallApply.receive(message(Name.func, partialAdd, origin));
        functionCallApply.receive(message(Name.a, _3, origin));
        functionCallApply.returnTo(resultListener, Name.result);

        result.setValue(0);
        await(() -> result.value != 0);
        assertEquals(Integer.valueOf(4), result.value);

        partialAdd.popPartialAppValues(origin);
    }

    private void assertResult(Integer expected, Integer a, FunctionCall<Integer> inc, Int result, Origin origin) {
        result.setValue(0);
        inc.receive(message(Name.a, a, origin));
        await(() -> result.value != 0);
        assertEquals(expected, result.value);

    }


    @Ignore
    @Test
    public void testQuicksort() {
         Actresses.trace();
         Actresses.debug();

        Function<List<Integer>> qsortCall = functionCall(getQuicksortSignature());
        qsortCall.label("qsortCall");

        //       checkQsort(Collections.emptyList(), Collections.emptyList(), qsortCall);
//        checkQsort(list(2), list(2), qsortCall);
//               checkQsort(list(1, 2), list(2, 1), qsortCall);
        // checkQsort(list(1, 2, 3), list(3, 2, 1), qsortCall);
        checkQsort(list(0, 1, 2, 3, 4), list( 3, 0, 2, 1, 4), qsortCall);

        //  checkQsort(list(0, 1, 2, 3), list(3, 2, 1, 0), qsortCall);


        //  checkQsort(list(0,1, 2, 3, 4, 5, 6), list(6, 5, 4, 3, 2, 1, 0), qsortCall);

        // checkQsort(list(0, 1, 2, 3, 4, 5), list(0, 1, 2, 3, 4, 5), qsortCall);
        // checkQsort(list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), list(9, 8, 7, 6, 5, 4, 3, 2, 1, 0), qsortCall);
        // checkQsort(list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), qsortCall);
        //   checkQsort(list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), list(6, 7, 8, 2, 3, 4, 5, 9, 1, 0), qsortCall);
    }

    private void checkQsort(List<Integer> expected, List<Integer> input, Function<List<Integer>> qsortCall) {
        ArrayList<Integer> result = new ArrayList<>();
        Int lastInt = Int(-1);
        Action resultListener = action(i -> {
            result.addAll((List<Integer>) i.value);
            lastInt.value = 1;
        }).param(Name.result);
        resultListener.propagate(Name.list, Name.list, qsortCall);

        qsortCall.returnTo(resultListener, Name.result);

        Origin origin = createSource(nop);
        resultListener.receive(message(Name.list, input, origin));

        await(() -> expected.size() > 0 ? result.size() == expected.size() : lastInt.value > 0);
        result.sort(Integer::compareTo);
        assertEquals(expected, result);
    }


    @Test
    public void testFilter() {
        /*
            input = List(1,2,3,4,5,6,7)
            check(filter(input, a -> iff (a == null) {false} else {a mod 2 == 0}))
            */

        Function<List<Integer>> filterCall = functionCall(filterSignature().get());
        filterCall.label("filter");
        FunctionSignature<Boolean> predicate = getModEqZero();
        predicate.constant(Name.m, 2);
        checkFilter(Collections.emptyList(), Collections.emptyList(), filterCall, predicate);
        checkFilter(list(2), list(2), filterCall, predicate);
        checkFilter(list(2, 2), list(2, 2), filterCall, predicate);
        checkFilter(emptyList(), list(1), filterCall, predicate);
        checkFilter(emptyList(), list(1, 1), filterCall, predicate);
        checkFilter(list(2), list(1, 2, 3), filterCall, predicate);
        checkFilter(list(0, 2, 4, 6, 8), list(6, 7, 8, 2, 3, 4, 5, 9, 1, 0), filterCall, predicate);
    }

    private List<Integer> list(Integer... i) {
        return asList(i);
    }

    private List<Integer> emptyList() {
        return Collections.emptyList();
    }

    private void checkFilter(List<Integer> expected, List<Integer> input, Function<List<Integer>> filterCall, FunctionSignature<Boolean> predicate) {
        ArrayList<Integer> result = new ArrayList<>();
        Int lastInt = Int(-1);
        Action resultListener = action(i -> {
            result.addAll((List<Integer>) i.value);
            lastInt.value = 1;
        }).param(Name.result);
        resultListener.propagate(Name.list, Name.list, filterCall);
        resultListener.propagate(Name.predicate, Name.predicate, filterCall);
        filterCall.returnTo(resultListener, Name.result);

        Origin origin = createSource(nop);
        resultListener.receive(message(Name.list, input, origin));
        resultListener.receive(message(Name.predicate, predicate, origin));

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
        Action resultListener = action(i -> result.setValue((Integer) i.value));

        resultListener.param(Name.result);
        Function<Integer> add = add()
                .returnTo(resultListener, Name.result)
                .constant(Name.leftArg, 1)
                .constant(Name.rightArg, 2);

        resultListener.kickOff(add);
        assertTrue(resultListener.executionStates.isEmpty());
        assertTrue(add.executionStates.isEmpty());

        Origin origin = createSource(resultListener);
        resultListener.receive(message(Name.kickOff, null, origin));

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
        Action resultListener = action(i -> result.setValue((Integer) i.value));

        Function<Integer> mul = BinOps.mul();
        Function<Integer> functionSignature = functionSignature(mul);

        Function<Integer> constOne = new Const(_1).returnTo(functionSignature, Name.a);

        Function<Integer> add = add()
                .returnTo(functionSignature, Name.b)
                .constant(Name.rightArg, _2);

        functionSignature
                .kickOff(constOne) // const must be dependent too but needs no propagations
                .propagate(Name.a, Name.leftArg, add)
                .propagate(Name.b, Name.leftArg, mul)
                .propagate(Name.b, Name.rightArg, mul);

        Function<Integer> functionCall = functionCall(functionSignature)
                .returnTo(resultListener, Name.result);
        resultListener.kickOff(functionCall);
        resultListener.param(Name.result);

        resultListener.receive(message(Name.kickOff, null, createSource(resultListener)));

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
        Action resultMonitor = action(i -> result.setValue((int) i.value));

        Const constOne = constant(1);
        Function<Integer> callee = functionSignature(constOne);
        Function<Integer> caller = functionCall(callee);
        caller.returnTo(resultMonitor, Name.result);
        resultMonitor.kickOff(caller);

        caller.kickOff(callee);
        callee.kickOff(constOne);

        resultMonitor.param(Name.result);

        resultMonitor.receive(message(Name.kickOff, null, createSource(resultMonitor)));
        await(() -> result.value != 0);

        assertEquals(_1, result.value);

    }

    private Origin createSource(Function a) {
        return Origin.origin(a, nop, 0L, 0, 0L);
    }

    @Test
    public void testFunctionCall() {

        /*

            function add(a, b) = (a + b);

            check(add(1, 2)) // run in ...
            check(add(3, 4)) // ... parallel

         */

        Map<String, Integer> results = new HashMap<>();

        Function<Integer> add = add();

        Function<Integer> functionAdd = functionSignature(add);
        functionAdd.label("add");
        functionAdd.propagate(Name.a, Name.leftArg, add);
        functionAdd.propagate(Name.b, Name.rightArg, add);

        Action resultMonitor = action(i -> results.put(i.key, (int) i.value));

        Function<Integer> callerA = functionCall(functionAdd).returnTo(resultMonitor, "resultA");
        Function<Integer> callerB = functionCall(functionAdd).returnTo(resultMonitor, "resultB");

        resultMonitor.param(callerA.returnKey);
        resultMonitor.param(callerB.returnKey);

        Origin originA = Origin.origin(callerA, nop, 0L, 0, 0L);
        callerA.receive(message(Name.a, 1, originA));
        callerA.receive(message(Name.b, 2, originA));

        Origin originB = Origin.origin(callerB, nop, 1L, 0, 0L);
        callerB.receive(message(Name.a, 3, originB));
        callerB.receive(message(Name.b, 4, originB));

        await(() -> results.size() == 2);
        assertEquals(results.get(callerA.returnKey), _3);
        assertEquals(results.get(callerB.returnKey), Integer.valueOf(7));
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

        Action resultMonitor = action(i -> result.setValue((int) i.value)).param(Name.result);
        resultMonitor.propagate(Name.a, Name.a, mul);
        mul.returnTo(resultMonitor, Name.result);
        mul.propagate(Name.a, Name.a, callerL);
        mul.propagate(Name.a, Name.a, callerR);

        Origin origin = standardOrigin(resultMonitor, 0L);
        resultMonitor.receive(message(Name.a, _1, origin));

        await(() -> result.value == 4);

    }


    @Test
    public void testNestedFunctionCall() {

        /*

            function pow2(a) = a * a;

            check(pow2(pow2(a)))

         */
        Int result = Int(0);

        Function<Integer> mul = BinOps.mul();
        Function<Integer> mulSignature = functionSignature(mul);
        mulSignature.propagate(Name.a, Name.leftArg, mul);
        mulSignature.propagate(Name.a, Name.rightArg, mul);

        Function<Integer> innerCaller = functionCall(mulSignature).constant(Name.a, 2);
        Function<Integer> outerCaller = functionCall(mulSignature);
        innerCaller.returnTo(outerCaller, Name.a);

        Action resultMonitor = action(i -> result.setValue((int) i.value));
        resultMonitor.param(Name.result);
        outerCaller.returnTo(resultMonitor, Name.result);

        mul.label("mul");
        mulSignature.label("MUL");
        resultMonitor.label("gateway");
        outerCaller.label("outerCaller");
        innerCaller.label("innerCaller");

        Origin origin = standardOrigin(resultMonitor, 0L);
        innerCaller.receive(message(Name.a, _2, origin));

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
        Action resultMonitor = action(i -> result.setValue((int) i.value));
        resultMonitor.param(Name.result);

        Function<Boolean> gt = BinOps.gt().returnTo(_if, Name.condition);
        Function subT = BinOps.sub().returnTo(_if, Name.onTrue);
        Function subF = BinOps.sub().returnTo(_if, Name.onFalse);

        _if.returnTo(resultMonitor, Name.result);

        _if.propagate(Name.a, Name.leftArg, gt);
        _if.propagate(Name.b, Name.rightArg, gt);
        _if.propagate(Name.a, Name.leftArg, subT);
        // propagation in this case can, but must not go through if
        resultMonitor.propagate(Name.b, Name.rightArg, subT);
        resultMonitor.propagate(Name.b, Name.leftArg, subF);
        resultMonitor.propagate(Name.a, Name.rightArg, subF);

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
        Origin originA = standardOrigin(resultMonitor, 0L);
        resultMonitor.receive(message(Name.a, _4, originA));
        resultMonitor.receive(message(Name.b, _1, originA));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);

        // False-path
        result.setValue(0);
        Origin originB = standardOrigin(resultMonitor, 1L);
        resultMonitor.receive(message(Name.a, _1, originB));
        resultMonitor.receive(message(Name.b, _4, originB));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        result.setValue(0);

    }


    @Test
    public void testSequentialIf() {

        /* The darn sequential if requires specific control of branch initializations and propagations
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
        Action resultMonitor = action(i -> result.setValue((int) i.value));
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
        Origin computationA = standardOrigin(resultMonitor, 0L);
        resultMonitor.receive(message(Name.a, _4, computationA));
        resultMonitor.receive(message(Name.b, _1, computationA));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);

        // False-path
        result.setValue(0);
        Origin computationB = standardOrigin(resultMonitor, 1L);
        resultMonitor.receive(message(Name.a, _1, computationB));
        resultMonitor.receive(message(Name.b, _4, computationB));
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
        Action resultMonitor = action(i -> result.setValue((int) i.value));
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
        Origin originA = standardOrigin(resultMonitor, 0L);
        resultMonitor.receive(message(Name.a, _4, originA));
        resultMonitor.receive(message(Name.b, _1, originA));
        await(() -> result.value != 0);
        assertEquals(_4, result.value);

        // False-path
        result.setValue(0);
        Origin originB = standardOrigin(resultMonitor, 1L);
        resultMonitor.receive(message(Name.a, _1, originB));
        resultMonitor.receive(message(Name.b, _4, originB));
        await(() -> result.value != 0);
        assertEquals(_4, result.value);
        result.setValue(0);

    }

    @Test
    public void testRecursion() {

        /* function sum(a) = if (a = 0 )
                                        0
                                   else
                                        a + sum(a - 1);
            echo(sum(3));
         */

        Int result = Int(0);
        Action resultMonitor = action(i -> result.setValue((int) i.value));
        resultMonitor.param(Name.result);

        Iff<Integer> _if = iff();
        _if.constant(Name.onTrue, _0);

        Function<Boolean> eq = BinOps.eq().constant(Name.rightArg, _0);
        Function<Integer> sub = BinOps.sub().constant(Name.rightArg, _1);
        Function<Integer> add = add();
        Function<Integer> sumSignature = functionSignature(_if);
        Function<Integer> sumReCall = functionCall(sumSignature);
        Function<Integer> sumCall = functionCall(sumSignature);

        resultMonitor.propagate(Name.a, Name.a, sumCall);

        sumSignature.propagate(Name.a, Name.a, _if);
        _if.propagate(Name.a, Name.leftArg, eq);
        _if.propagateOnFalse(Name.a, Name.leftArg, add);
        _if.propagateOnFalse(Name.a, Name.a, add);
        // can't propagate "a" to sumCallRec directly, so that it passes "a" on to sub ("a - 1") alone. it doesen't.
        // instead it causes another recursion with "a" alone rather than "a - 1" from sub (and maybe more mess, because
        // sum(a) will also receive a second "a" from the "a - 1", but that doesen't matter any more then)
        // one would have to distinguish somehow by key the "a" as the parameter in sum(a) from the a in "a - 1"
        // -> so this doesen't work:
        // mul.propagate(Name.a, Name.a, sumCallRec);
        add.propagate(Name.a, Name.leftArg, sub);

        //TODO: the returning structure is symmetrical to the dependency structure, maybe setting up both can be unified
        eq.returnTo(_if, Name.condition);
        sub.returnTo(sumReCall, Name.a);
        sumReCall.returnTo(add, Name.rightArg);
        add.returnTo(_if, Name.onFalse);
        sumCall.returnTo(resultMonitor, Name.result);

        int runId = 0;
        checksum(result, resultMonitor, runId++, 0, 0);
        checksum(result, resultMonitor, runId++, 1, 1);
        checksum(result, resultMonitor, runId++, 2, 3);
        checksum(result, resultMonitor, runId++, 1000, 1001 * 500);
        checksum(result, resultMonitor, runId++, 7000, 7001 * 3500);
        //checksum(result, resultMonitor, runId, 64000, 64001 * 32000); // close to MaxInt, isn't overflow-save

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
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    //
                }
                next = input.poll();
            }
        });
    }

    private void checksum(Int result, Action resultMonitor, int runId, int sumOf, Integer expected) {
        Long start = System.nanoTime();

        result.value = -1;

        Origin run = standardOrigin(resultMonitor, (long) runId);
        resultMonitor.receive(message(Name.a, sumOf, run));

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


    private Origin standardOrigin(Action gateway, Long runId) {
        return origin(gateway, nop, runId, 0, 0L);
    }

}