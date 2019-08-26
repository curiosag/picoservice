package miso;

import miso.ingredients.*;
import miso.misc.Name;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static miso.ingredients.Action.action;
import static miso.ingredients.BinOp.add;
import static miso.ingredients.BinOp.sub;
import static miso.ingredients.Call.call;
import static miso.ingredients.CallSync.sync;
import static miso.ingredients.CallTarget.callTarget;
import static miso.ingredients.Const.constant;
import static miso.ingredients.BinOp.eq;
import static miso.ingredients.BinOp.gt;
import static miso.ingredients.BinOp.mul;
import static miso.ingredients.Iff.iff;
import static miso.ingredients.Message.message;
import static miso.ingredients.Source.source;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServiceTest {
    private static final Integer _5 = 5;
    private static final Integer _4 = 4;
    private static final Integer _3 = 3;
    private static final Integer _2 = 2;
    private static final Integer _1 = 1;
    private static final Integer _0 = 0;

    private class Int {
        public Integer value;

        public void setValue(Integer value) {
            this.value = value;
        }

        public Int(Integer value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Int anInt = (Int) o;
            return value.equals(anInt.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    Int Int(int i) {
        return new Int(i);
    }

    /*
    quickSort []     = []                           -- Sort an empty list
    quickSort (x:xs) = quickSort (filter (<x) xs)   -- Sort the left part of the list
                   ++ [x] ++                    -- Insert pivot between two sorted parts
                   quickSort (filter (>=x) xs)  -- Sort the right part of the list

    */

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

        Actress.shutdown();
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

        Source source = createSource(resultListener);
        resultListener.recieve(message(Name.kickOff, null, source));

        await(() -> add.executionStates.size() > 0);
        assertEquals(1, resultListener.executionStates.size());
        assertEquals(1, add.executionStates.size());

        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        resultListener.recieve(message(Name.finalizeComputation, null, source));
        await(() -> add.executionStates.size() < 1);

        assertEquals(0, resultListener.executionStates.size());
        assertEquals(0, add.executionStates.size());

        Actress.shutdown();
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

        Function<Integer> mul = mul();  // mul's return key and dependency are handled in CallTarget constructor
        Function<Integer> functionCallTarget = callTarget(mul);

        Function<Integer> constOne = new Const(_1).returnTo(functionCallTarget, Name.a);

        Function<Integer> add = add()
                .returnTo(functionCallTarget, Name.b)
                .constant(Name.rightArg, _2);


        functionCallTarget
                .kickOff(constOne) // const must be dependent too but needs no propagations
                .kickOff(add)
                .kickOff(mul)
                .propagate(Name.a, Name.leftArg, add)
                .propagate(Name.b, Name.leftArg, mul)
                .propagate(Name.b, Name.rightArg, mul);

        Function<Integer> functionCall = call(functionCallTarget)
                .returnTo(resultListener, Name.result);
        resultListener.kickOff(functionCall);
        resultListener.param(Name.result);

        resultListener.recieve(message(Name.kickOff, null, createSource(resultListener)));

        await(() -> result.value != 0);
        assertEquals(Integer.valueOf(9), result.value);

        Actress.shutdown();
    }

    @Test
    public void testFunctionCallNoParams() {

        /*

            function get() = 1;

            print(get())

         */
        Int result = Int(0);
        Action resultReceiver = action(i -> result.setValue((int) i.value));

        Const constOne = constant(1);
        Function<Integer> callee = callTarget(constOne);
        Function<Integer> caller = call(callee);
        caller.returnTo(resultReceiver, Name.result);
        resultReceiver.kickOff(caller);

        caller.kickOff(callee);
        callee.kickOff(constOne);

        resultReceiver.param(Name.result);

        resultReceiver.recieve(message(Name.kickOff, null, createSource(resultReceiver)));
        await(() -> result.value != 0);
        resultReceiver.recieve(message(Name.finalizeComputation, null, createSource(resultReceiver)));

        assertEquals(_1, result.value);

        Actress.shutdown();
    }

    private Source createSource(Action printMsg) {
        return source(printMsg, 0L, 0);
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
        Function<Integer> callee = callTarget(add);
        callee.propagate(Name.a, Name.leftArg, add);
        callee.propagate(Name.b, Name.rightArg, add);


        Function<Integer> callerA = call(callee);
        Function<Integer> callerB = call(callee);

        String resultCallA = "resultCallA";
        String resultCallB = "resultCallB";

        Action resultReceiver = action(i -> results.put(i.key, (int) i.value));

        callerA.returnTo(resultReceiver, resultCallA);
        callerB.returnTo(resultReceiver, resultCallB);

        resultReceiver.param(resultCallA);
        resultReceiver.param(resultCallB);

        Source sourceA = source(resultReceiver, 0L, 0);
        callerA.recieve(message(Name.a, 1, sourceA));
        callerA.recieve(message(Name.b, 2, sourceA));

        Source sourceB = source(resultReceiver, 1L, 0);
        callerB.recieve(message(Name.a, 3, sourceB));
        callerB.recieve(message(Name.b, 4, sourceB));


        await(() -> results.size() == 2);
        assertEquals(results.get(resultCallA), _3);
        assertEquals(results.get(resultCallB), Integer.valueOf(7));

        Actress.shutdown();
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
        Action resultReceiver = action(i -> result.setValue((int) i.value));
        resultReceiver.param(Name.result);

        Function<Boolean> gt = gt().returnTo(_if, Name.condition);
        Function subT = sub().returnTo(_if, Name.onTrue);
        Function subF = sub().returnTo(_if, Name.onFalse);

        _if.returnTo(resultReceiver, Name.result);

        _if.propagate(Name.a, Name.leftArg, gt);
        _if.propagate(Name.b, Name.rightArg, gt);
        _if.propagate(Name.a, Name.leftArg, subT);
        // propagation in this case can, but must not go through if
        resultReceiver.propagate(Name.b, Name.rightArg, subT);
        resultReceiver.propagate(Name.b, Name.leftArg, subF);
        resultReceiver.propagate(Name.a, Name.rightArg, subF);

        resultReceiver.propagate(Name.a, Name.a, _if);
        resultReceiver.propagate(Name.b, Name.b, _if);

        resultReceiver.kickOff(_if);
        _if.kickOff(gt);
        _if.kickOff(subT);
        _if.kickOff(subF);

        // True-path
        Source sourceA = source(resultReceiver, 0L, 0);
        resultReceiver.recieve(message(Name.kickOff, null, sourceA));
        resultReceiver.recieve(message(Name.a, _4, sourceA));
        resultReceiver.recieve(message(Name.b, _1, sourceA));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        resultReceiver.recieve(message(Name.finalizeComputation, null, sourceA));

        // False-path
        result.setValue(0);
        Source sourceB = source(resultReceiver, 1L, 0);
        resultReceiver.recieve(message(Name.kickOff, null, sourceB));
        resultReceiver.recieve(message(Name.a, _1, sourceB));
        resultReceiver.recieve(message(Name.b, _4, sourceB));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        result.setValue(0);
        resultReceiver.recieve(message(Name.finalizeComputation, null, sourceB));


        Actress.shutdown();
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
        Action resultReceiver = action(i -> result.setValue((int) i.value));
        resultReceiver.param(Name.result);

        Function<Boolean> gt = gt()
                .returnTo(iff, Name.condition);
        Function subT = sub()
                .returnTo(iff, Name.onTrue);
        Function subF = sub()
                .returnTo(iff, Name.onFalse);

        iff.returnTo(resultReceiver, Name.result);
        // condition may propagate, init and finalize as usual
        iff.propagate(Name.a, Name.leftArg, gt);
        iff.propagate(Name.b, Name.rightArg, gt);
        iff.kickOff(gt);
        // true and false branches must propagate, init and finalize differently, and also separately from the condition
        iff.propagateOnTrue(Name.a, Name.leftArg, subT);
        iff.propagateOnTrue(Name.b, Name.rightArg, subT);
        iff.kickOffOnTrue(subT);

        iff.propagateOnFalse(Name.b, Name.leftArg, subF);
        iff.propagateOnFalse(Name.a, Name.rightArg, subF);
        iff.kickOffOnFalse(subF);

        resultReceiver.kickOff(iff);
        resultReceiver.propagate(Name.a, Name.a, iff);
        resultReceiver.propagate(Name.b, Name.b, iff);

        // True-path
        Source computationA = source(resultReceiver, 0L, 0);
        resultReceiver.recieve(message(Name.kickOff, null, computationA));
        resultReceiver.recieve(message(Name.a, _4, computationA));
        resultReceiver.recieve(message(Name.b, _1, computationA));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        resultReceiver.recieve(message(Name.finalizeComputation, null, computationA));

        // False-path
        result.setValue(0);
        Source computationB = source(resultReceiver, 1L, 0);
        resultReceiver.recieve(message(Name.kickOff, null, computationB));
        resultReceiver.recieve(message(Name.a, _1, computationB));
        resultReceiver.recieve(message(Name.b, _4, computationB));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        result.setValue(0);
        resultReceiver.recieve(message(Name.finalizeComputation, null, computationB));

        Actress.shutdown();
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
        Action resultReceiver = action(i -> result.setValue((int) i.value));
        resultReceiver.param(Name.result);

        Function<Boolean> gt = gt();

        gt.returnTo(_if, Name.condition);

        _if.returnTo(resultReceiver, Name.result);
        _if.kickOff(gt);

        // an example how init/finalize could deviate from propagations, which reduces communication,
        // but may be confusing

        resultReceiver.propagate(Name.a, Name.leftArg, gt);
        resultReceiver.propagate(Name.b, Name.rightArg, gt);
        resultReceiver.propagate(Name.a, Name.onTrue, _if);
        resultReceiver.propagate(Name.b, Name.onFalse, _if);
        resultReceiver.kickOff(_if);

        // True-path
        Source sourceA = source(resultReceiver, 0L, 0);
        resultReceiver.recieve(message(Name.kickOff, null, sourceA));
        resultReceiver.recieve(message(Name.a, _4, sourceA));
        resultReceiver.recieve(message(Name.b, _1, sourceA));
        await(() -> result.value != 0);
        assertEquals(_4, result.value);
        resultReceiver.recieve(message(Name.finalizeComputation, null, sourceA));

        // False-path
        result.setValue(0);
        Source sourceB = source(resultReceiver, 1L, 0);
        resultReceiver.recieve(message(Name.kickOff, null, sourceB));
        resultReceiver.recieve(message(Name.a, _1, sourceB));
        resultReceiver.recieve(message(Name.b, _4, sourceB));
        await(() -> result.value != 0);
        assertEquals(_4, result.value);
        result.setValue(0);
        resultReceiver.recieve(message(Name.finalizeComputation, null, sourceB));

        Actress.shutdown();
    }

    @Test
    public void testRecursion() {

        /* function sum(a) = if (a = 1 )
                                        1
                                   else
                                        a + sum(a - 1);
            echo(sum(3));
         */

        Int result = Int(0);
        Action resultMonitor = action(i -> result.setValue((int) i.value));
        resultMonitor.param(Name.result);

        Iff<Integer> _if = iff();
        _if.constant(Name.onTrue, _1);

        Function<Boolean> eq = eq().constant(Name.rightArg, _1);
        Function<Integer> sub = sub().constant(Name.rightArg, _1);
        Function<Integer> add = add();
        Function<Integer> sumCallTarget = callTarget(_if);
        Function<Integer> sumCallRec = call(sumCallTarget);
        Function<Integer> sumCall = call(sumCallTarget);

        resultMonitor.propagate(Name.a, Name.a, sumCall);

        sumCallTarget.propagate(Name.a, Name.a, _if);
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
        sub.returnTo(sumCallRec, Name.a);
        sumCallRec.returnTo(add, Name.rightArg);
        add.returnTo(_if, Name.onFalse);
        sumCall.returnTo(resultMonitor, Name.result);

        resultMonitor.kickOff(sumCall);
        sumCallTarget.kickOff(_if);
        _if.kickOff(eq);
        _if.kickOffOnFalse(add);
        add.kickOff(sumCallRec);
        sumCallRec.kickOff(sub);

        int runId = 0;
        checksum(result, resultMonitor, runId++, 1, 1);
        checksum(result, resultMonitor, runId++, 2, 3);
        checksum(result, resultMonitor, runId++, 1000, 1001 * 500);
        checksum(result, resultMonitor, runId++, 7000, 7001 * 3500);
        checksum(result, resultMonitor, runId, 40000, 40001 * 20000);

        Actress.shutdown();
    }

    private void checksum(Int result, Action resultMonitor, int runId, int facOf, Integer expected) {
        Long start = System.nanoTime();

        result.value = 0;

        Source run = source(resultMonitor, (long) runId, 0);
        resultMonitor.recieve(message(Name.a, facOf, run));

        await(() -> result.value != 0);
        assertEquals(expected, result.value);
        resultMonitor.recieve(message(Name.finalizeComputation, null, run));

        Long stop = System.nanoTime();
        long elapsed = (stop - start) / 1000000;

        System.out.println(String.format("sum for %d took %d millis", facOf, elapsed));

    }

    private Integer sum(Integer i) {
        if (i == 1) {
            return 1;
        }
        return i + sum(i - 1);
    }

    @Test
    public void recursiveSumInJava() {
        Long start = System.nanoTime();
        Integer max = 7000;
        Integer sum = sum(max);
        Long stop = System.nanoTime();
        long elapsed = (stop - start) / 1000000;

        System.out.println(String.format("java function: sum of %d (%d) in %d millis", max, sum, elapsed));
    }

    private void await(Supplier<Boolean> condition) {
        while (!condition.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //
            }
        }
    }

}