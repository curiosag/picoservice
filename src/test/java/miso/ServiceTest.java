package miso;

import miso.ingredients.*;
import miso.message.Message;
import miso.message.Name;
import org.junit.Test;
import org.junit.rules.Stopwatch;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static miso.ingredients.Action.action;
import static miso.ingredients.Add.add;
import static miso.ingredients.CallSync.callSync;
import static miso.ingredients.Const.constVal;
import static miso.ingredients.Eq.eq;
import static miso.ingredients.Gt.gt;
import static miso.ingredients.Mul.mul;
import static miso.ingredients.Sub.sub;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServiceTest {
    private static final Integer _5 = 5;
    private static final Integer _4 = 4;
    private static final Integer _3 = 3;
    private static final Integer _2 = 2;
    private static final Integer _1 = 1;
    private static final Integer _0 = 0;

    private static Action printMsg = action(i -> System.out.println(i.toString()));

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
            return value == anInt.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    Int Int(int i) {
        return new Int(i);
    }

    static {
        new Thread(printMsg).start();
    }

    private Message message(String key, Object value) {
        return Message.of(key, value, createSource(printMsg));
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

        Function<Integer> add = start(add());

        assertEquals(_1, callSync(add)
                .param(Name.leftArg, 0)
                .param(Name.rightArg, 1)
                .call());

        add.addConst(Name.rightArg, 3);
        assertEquals(_5, callSync(add)
                .param(Name.leftArg, 2)
                .call());

        add.terminate();
    }


    @Test
    public void testCallSyncEmptyParams() {

        /*

            func f = 1; // or so

            sync(f);

         */

        assertEquals(_1, callSync(constVal(1)).call());
    }

    @Test
    public void testInitializeAndTerminateComputation_ConstValuesOnly() {

        /*
            print(1 + 2)
         */

        Int result = Int(0);

        Action resultListener = startAction(action(i -> result.setValue((Integer) i.value)));
        resultListener.expectParam(Name.result);
        Function<Integer> add = add()
                .returnTo(Name.result, resultListener)
                .addConst(Name.leftArg, 1)
                .addConst(Name.rightArg, 2);
        start(add);


        resultListener.addInitAndFinalize(add);

        assertTrue(resultListener.states.isEmpty());
        assertTrue(add.states.isEmpty());

        Source source = createSource(resultListener);
        resultListener.recieve(new Message(Name.initializeComputation, null, source));

        await(() -> add.states.size() > 0);
        assertEquals(1, resultListener.states.size());
        assertEquals(1, add.states.size());

        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        resultListener.recieve(new Message(Name.finalizeComputation, null, source));
        await(() -> add.states.size() < 1);

        assertEquals(0, resultListener.states.size());
        assertEquals(0, add.states.size());

        resultListener.terminate();
        add.terminate();
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
        Action resultListener = startAction(action(i -> result.setValue((Integer) i.value)));

        Function<Integer> mul = start(mul());  // mul's return key and dependency are handled in FunctionCallTarget constructor
        Function<Integer> functionCallTarget = start(new FunctionCallTarget<>(mul));

        Function<Integer> constOne = new Const(_1).returnTo(Name.a, functionCallTarget);

        Function<Integer> add = start(add())
                .returnTo(Name.b, functionCallTarget)
                .addConst(Name.rightArg, _2);


        functionCallTarget
                .addInitAndFinalize(constOne) // const must be dependent too but needs no propagations
                .addInitAndFinalize(add)
                .addInitAndFinalize(mul)
                .addPropagation(Name.a, Name.leftArg, add)
                .addPropagation(Name.b, Name.leftArg, mul)
                .addPropagation(Name.b, Name.rightArg, mul);

        Function<Integer> functionCall = start(new FunctionCall<>(functionCallTarget))
                .returnTo(Name.result, resultListener);
        resultListener.addInitAndFinalize(functionCall);
        resultListener.expectParam(Name.result);

        resultListener.recieve(new Message(Name.initializeComputation, null, createSource(resultListener)));

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
        Action resultReceiver = startAction(action(i -> result.setValue((int) i.value)));
        Const constOne = constVal(1);
        Function<Integer> callee = start(new FunctionCallTarget<>(constOne));
        Function<Integer> caller = start(new FunctionCall<>(callee));
        caller.returnTo(Name.result, resultReceiver);
        resultReceiver.addInitAndFinalize(caller);

        caller.addInitAndFinalize(callee);
        callee.addInitAndFinalize(constOne);

        resultReceiver.expectParam(Name.result);

        resultReceiver.recieve(new Message(Name.initializeComputation, null, createSource(resultReceiver)));
        await(() -> result.value != 0);
        resultReceiver.recieve(new Message(Name.finalizeComputation, null, createSource(resultReceiver)));

        assertEquals(_1, result.value);

        callee.terminate();
        caller.terminate();
        resultReceiver.terminate();
    }

    private Source createSource(Action printMsg) {
        return new Source(printMsg, 0L, 0);
    }

    @Test
    public void testFunctionCall() {

        /*

            function add(a, b) = (a + b);

            check(add(1, 2)) // run in ...
            check(add(3, 4)) // ... parallel

         */
        Map<String, Integer> results = new HashMap<>();

        Function<Integer> add = start(add());
        Function<Integer> callTarget = start(new FunctionCallTarget<>(add));
        callTarget.addPropagation(Name.a, Name.leftArg, add);
        callTarget.addPropagation(Name.b, Name.rightArg, add);


        Function<Integer> callAddA = start(new FunctionCall<>(callTarget));
        Function<Integer> callAddB = start(new FunctionCall<>(callTarget));

        String resultCallA = "resultCallA";
        String resultCallB = "resultCallB";

        Action resultReceiver = startAction(action(i -> results.put(i.key, (int) i.value)));

        callAddA.returnTo(resultCallA, resultReceiver);
        callAddB.returnTo(resultCallB, resultReceiver);

        resultReceiver.expectParam(resultCallA);
        resultReceiver.expectParam(resultCallB);

        Source sourceA = new Source(resultReceiver, 0L, 0);
        callAddA.recieve(new Message(Name.a, 1, sourceA));
        callAddA.recieve(new Message(Name.b, 2, sourceA));

        Source sourceB = new Source(resultReceiver, 1L, 0);
        callAddB.recieve(new Message(Name.a, 3, sourceB));
        callAddB.recieve(new Message(Name.b, 4, sourceB));


        await(() -> results.size() == 2);

        assertEquals(results.get(resultCallA), _3);
        assertEquals(results.get(resultCallB), Integer.valueOf(7));

        add.terminate();
        resultReceiver.terminate();
        callTarget.terminate();
        callAddA.terminate();
        callAddB.terminate();

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

        Function<Integer> _if = start(If.condInt());

        Int result = Int(0);
        Action resultReceiver = startAction(action(i -> result.setValue((int) i.value)));
        resultReceiver.expectParam(Name.result);

        Function<Boolean> gt = start(gt())
                .returnTo(Name.condition, _if);
        Function subT = start(sub())
                .returnTo(Name.onTrue, _if);
        Function subF = start(sub())
                .returnTo(Name.onFalse, _if);

        _if.returnTo(Name.result, resultReceiver);

        _if.addPropagation(Name.a, Name.leftArg, gt);
        _if.addPropagation(Name.b, Name.rightArg, gt);
        _if.addPropagation(Name.a, Name.leftArg, subT);
        // propagation in this case can, but must not go through if
        resultReceiver.addPropagation(Name.b, Name.rightArg, subT);
        resultReceiver.addPropagation(Name.b, Name.leftArg, subF);
        resultReceiver.addPropagation(Name.a, Name.rightArg, subF);

        resultReceiver.addPropagation(Name.a, Name.a, _if);
        resultReceiver.addPropagation(Name.b, Name.b, _if);

        // InitAndFinalize not explicitly needed here, since if, gt, subT and subF get messages anyway to get them going
        resultReceiver.addInitAndFinalize(_if);
        _if.addInitAndFinalize(gt);
        _if.addInitAndFinalize(subT);
        _if.addInitAndFinalize(subF);

        // True-path
        Source sourceA = new Source(resultReceiver, 0L, 0);
        resultReceiver.recieve(new Message(Name.initializeComputation, null, sourceA));
        resultReceiver.recieve(new Message(Name.a, _4, sourceA));
        resultReceiver.recieve(new Message(Name.b, _1, sourceA));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        resultReceiver.recieve(new Message(Name.finalizeComputation, null, sourceA));

        // False-path
        result.setValue(0);
        Source sourceB = new Source(resultReceiver, 1L, 0);
        resultReceiver.recieve(new Message(Name.initializeComputation, null, sourceB));
        resultReceiver.recieve(new Message(Name.a, _1, sourceB));
        resultReceiver.recieve(new Message(Name.b, _4, sourceB));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        result.setValue(0);
        resultReceiver.recieve(new Message(Name.finalizeComputation, null, sourceB));


        _if.terminate();
        gt.terminate();
        subF.terminate();
        subT.terminate();
        resultReceiver.terminate();

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

        SIf<Integer> _if = start(SIf.condInt());

        Int result = Int(0);
        Action resultReceiver = startAction(action(i -> result.setValue((int) i.value)));
        resultReceiver.expectParam(Name.result);

        Function<Boolean> gt = start(gt())
                .returnTo(Name.condition, _if);
        Function subT = start(sub())
                .returnTo(Name.onTrue, _if);
        Function subF = start(sub())
                .returnTo(Name.onFalse, _if);

        _if.returnTo(Name.result, resultReceiver);
        // condition may propagate, init and finalize as usual
        _if.addPropagation(Name.a, Name.leftArg, gt);
        _if.addPropagation(Name.b, Name.rightArg, gt);
        _if.addInitAndFinalize(gt);
        // true and false branches must propagate, init and finalize differently, and also separately from the condition
        _if.addPropagationOnTrue(Name.a, Name.leftArg, subT);
        _if.addPropagationOnTrue(Name.b, Name.rightArg, subT);
        _if.addInitAndFinalizeOnTrue(subT);

        _if.addPropagationOnFalse(Name.b, Name.leftArg, subF);
        _if.addPropagationOnFalse(Name.a, Name.rightArg, subF);
        _if.addInitAndFinalizeOnFalse(subF);

        resultReceiver.addInitAndFinalize(_if);
        resultReceiver.addPropagation(Name.a, Name.a, _if);
        resultReceiver.addPropagation(Name.b, Name.b, _if);

        // True-path
        Source computationA = new Source(resultReceiver, 0L, 0);
        resultReceiver.recieve(new Message(Name.initializeComputation, null, computationA));
        resultReceiver.recieve(new Message(Name.a, _4, computationA));
        resultReceiver.recieve(new Message(Name.b, _1, computationA));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        resultReceiver.recieve(new Message(Name.finalizeComputation, null, computationA));

        // False-path
        result.setValue(0);
        Source computationB = new Source(resultReceiver, 1L, 0);
        resultReceiver.recieve(new Message(Name.initializeComputation, null, computationB));
        resultReceiver.recieve(new Message(Name.a, _1, computationB));
        resultReceiver.recieve(new Message(Name.b, _4, computationB));
        await(() -> result.value != 0);
        assertEquals(_3, result.value);
        result.setValue(0);
        resultReceiver.recieve(new Message(Name.finalizeComputation, null, computationB));

        _if.terminate();
        gt.terminate();
        subF.terminate();
        subT.terminate();
        resultReceiver.terminate();

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

        Function<Integer> _if = SIf.condInt();
        start(_if);

        Int result = Int(0);
        Action resultReceiver = startAction(action(i -> result.setValue((int) i.value)));
        resultReceiver.expectParam(Name.result);

        Function<Boolean> gt = start(gt());

        gt.returnTo(Name.condition, _if);

        _if.returnTo(Name.result, resultReceiver);
        _if.addInitAndFinalize(gt);

        // an example how init/finalize could deviate from propagations, which reduces communication,
        // but may be confusing

        resultReceiver.addPropagation(Name.a, Name.leftArg, gt);
        resultReceiver.addPropagation(Name.b, Name.rightArg, gt);
        resultReceiver.addPropagation(Name.a, Name.onTrue, _if);
        resultReceiver.addPropagation(Name.b, Name.onFalse, _if);
        resultReceiver.addInitAndFinalize(_if);

        // True-path
        Source sourceA = new Source(resultReceiver, 0L, 0);
        resultReceiver.recieve(new Message(Name.initializeComputation, null, sourceA));
        resultReceiver.recieve(new Message(Name.a, _4, sourceA));
        resultReceiver.recieve(new Message(Name.b, _1, sourceA));
        await(() -> result.value != 0);
        assertEquals(_4, result.value);
        resultReceiver.recieve(new Message(Name.finalizeComputation, null, sourceA));

        // False-path
        result.setValue(0);
        Source sourceB = new Source(resultReceiver, 1L, 0);
        resultReceiver.recieve(new Message(Name.initializeComputation, null, sourceB));
        resultReceiver.recieve(new Message(Name.a, _1, sourceB));
        resultReceiver.recieve(new Message(Name.b, _4, sourceB));
        await(() -> result.value != 0);
        assertEquals(_4, result.value);
        result.setValue(0);
        resultReceiver.recieve(new Message(Name.finalizeComputation, null, sourceB));

        _if.terminate();
        gt.terminate();
        resultReceiver.terminate();

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
        Action resultMonitor = startAction(action(i -> result.setValue((int) i.value)));
        resultMonitor.expectParam(Name.result);

        SIf<Integer> _if = start(SIf.condInt());
        _if.addConst(Name.onTrue, _1);

        Function<Boolean> eq = start(eq()).addConst(Name.rightArg, _1);
        Function<Integer> sub = start(sub()).addConst(Name.rightArg, _1);
        Function<Integer> add = start(add());
        Function<Integer> sumCallTarget = start(new FunctionCallTarget<>(_if));
        Function<Integer> sumCallRec = start(new FunctionCall<>(sumCallTarget));
        Function<Integer> sumCall = start(new FunctionCall<>(sumCallTarget));

        resultMonitor.addPropagation(Name.a, Name.a, sumCall);

        sumCallTarget.addPropagation(Name.a, Name.a, _if);
        _if.addPropagation(Name.a, Name.leftArg, eq);
        _if.addPropagationOnFalse(Name.a, Name.leftArg, add);
        _if.addPropagationOnFalse(Name.a, Name.a, add);
        // can't propagate "a" to sumCallRec directly, so that it passes "a" on to sub ("a - 1") alone. it doesen't.
        // instead it causes another recursion with "a" alone rather than "a - 1" from sub (and maybe more mess, because
        // sum(a) will also receive a second "a" from the "a - 1", but that doesen't matter any more then)
        // one would have to distinguish somehow by key the "a" as the parameter in sum(a) from the a in "a - 1"
        // -> so this doesen't work:
        // mul.addPropagation(Name.a, Name.a, sumCallRec);
        add.addPropagation(Name.a, Name.leftArg, sub);

        //TODO: the returning structure is symmetrical to the dependency structure, maybe setting up both can be unified
        eq.returnTo(Name.condition, _if);
        sub.returnTo(Name.a, sumCallRec);
        sumCallRec.returnTo(Name.rightArg, add);
        add.returnTo(Name.onFalse, _if);
        sumCall.returnTo(Name.result, resultMonitor);

        resultMonitor.addInitAndFinalize(sumCall);
        sumCallTarget.addInitAndFinalize(_if);
        _if.addInitAndFinalize(eq);
        _if.addInitAndFinalizeOnFalse(add);
        add.addInitAndFinalize(sumCallRec);
        sumCallRec.addInitAndFinalize(sub);

        int runId = 0;
        checksum(result, resultMonitor, runId++, 1, 1);
        checksum(result, resultMonitor, runId++, 2, 3);
        checksum(result, resultMonitor, runId++, 1000, 1001 * 500);
        checksum(result, resultMonitor, runId++, 10000, 10001 * 5000);
        checksum(result, resultMonitor, runId, 40000, 40001 * 20000);

        resultMonitor.terminate();
        _if.terminate();
        eq.terminate();
        sub.terminate();
        add.terminate();
        sumCallTarget.terminate();
        sumCallRec.terminate();
        sumCall.terminate();
    }

    private void checksum(Int result, Action resultMonitor, int runId, int facOf, Integer expected) {
        Instant start = Instant.now();

        result.value = 0;

        Source run = new Source(resultMonitor, (long) runId, 0);
        resultMonitor.recieve(new Message(Name.a, facOf, run));

        await(() -> result.value != 0);
        assertEquals(expected, result.value);
        resultMonitor.recieve(new Message(Name.finalizeComputation, null, run));
        System.out.println(String.format("sum for %d took %d seconds", facOf, Duration.between(start, Instant.now()).getSeconds()));

    }

    private void await(Supplier<Boolean> condition) {
        while (!condition.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //
            }
        }
    }

    private Eq start(Eq f) {
        new Thread(f).start();
        return f;
    }

    private Function<Boolean> start(Gt f) {
        new Thread(f).start();
        return f;
    }

    private Function<Integer> start(Function<Integer> f) {
        new Thread(f).start();
        return f;
    }

    private Action startAction(Action f) {
        new Thread(f).start();
        return f;
    }

    private FunctionCallTarget<Integer> start(FunctionCallTarget<Integer> f) {
        new Thread(f).start();
        return f;
    }

    private If<Integer> start(If<Integer> f) {
        new Thread(f).start();
        return f;
    }

    private SIf<Integer> start(SIf<Integer> f) {
        new Thread(f).start();
        return f;
    }
}