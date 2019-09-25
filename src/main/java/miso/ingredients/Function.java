package miso.ingredients;

import miso.ingredients.tuples.KeyValuePair;
import miso.ingredients.tuples.OnReturnForwardItem;
import miso.ingredients.tuples.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static miso.ingredients.Message.message;

public abstract class Function<T> extends Actress {
    public Function<?> returnTo;
    public String returnKey;
    final List<OnReturnForwardItem> onReturn = new ArrayList<>();
    private final List<OnReturnForwardItem> onReturnReceived = new ArrayList<>();

    public final Map<FunctionCallTreeLocation, State> executionStates = new HashMap<>();
    private final Map<String, Object> consts = new HashMap<>();

    protected void removeState(Origin origin) {
        executionStates.remove(origin.functionCallTreeLocation());
        //debug(String.format("-->  %s:%d States. Removed (%d/%d) %s ", address.toString(), executionStates.size(), origin.executionId, origin.callLevel, origin.sender.address.toString()));
    }

    void cleanup(Long runId) {
        List<FunctionCallTreeLocation> toRemove = executionStates.keySet().stream()
                .filter(k -> k.getExecutionId().equals(runId))
                .collect(Collectors.toList());
        toRemove.forEach(executionStates::remove);
    }

    public void onReturnSend(String key, Object value, Function<?> target) {
        onReturn.add(OnReturnForwardItem.of(KeyValuePair.of(key, value), target));
    }

    public void onReturnReceivedSend(String key, Object value, Function target) {
        onReturnReceived.add(OnReturnForwardItem.of(KeyValuePair.of(key, value), target));
    }

    @Override
    public void checkSanityOnStop() {
        super.checkSanityOnStop();
        if (!executionStates.isEmpty()) {
            System.out.println(String.format("-->  %s %s: %d execution states left after stop", address.toString(), address.toString(), executionStates.size()));
            executionStates.clear();
        }
        if (!inBox.isEmpty()) {
            System.out.println(String.format("%s: %d left in inbox", address.toString(), inBox.size()));
            inBox.clear();
        }
    }

    protected abstract State newState(Origin origin);


    /*
     *   propagation data structure:
     *
     *   Map<keyReceived , List<Tuple<List<keysToPropagate>,targetFunc>>
     *
     *   all targetFunc know the keyValue used for returning results
     *
     * */
    protected final Map<String, List<Tuple<String, Function<?>>>> propagations = new HashMap<>();

    private List<Function> kicks = new ArrayList<>();

    protected State getState(Origin origin) {
        State result = executionStates.get(origin.functionCallTreeLocation());
        if (result == null) {
            result = newState(origin);
            executionStates.put(origin.functionCallTreeLocation(), result);
            fowardConsts(origin);
            forwardKickOff(origin);
        }
        return result;
    }

    protected void fowardConsts(Origin origin) {
        consts.forEach((key, value) -> receive(message(key, value, origin.sender(this))));
    }

    public Function<T> kickOff(Function target) {
        kicks.add(target);
        return this;
    }

    private void forwardKickOff(Origin origin) {
        kicks.forEach(p -> p.receive(message(Name.kickOff, null, origin.sender(this))));
    }

    public Function<T> returnTo(Function<?> f, String returnKey) {
        returnTo = f;
        this.returnKey = returnKey;
        return this;
    }

    void propagate(String keyReceived, String keyToPropagate, Function targetFunc, Map<String, List<Tuple<String, Function<?>>>>  propagations) {
        List<Tuple<String, Function<?>>> targets = propagations.computeIfAbsent(keyReceived, k -> new ArrayList<>());

        if (targets.stream().noneMatch(i -> i.left.equals(keyToPropagate) && i.right.equals(targetFunc))){
            targets.add(new Tuple<String, Function<?>>(keyToPropagate, targetFunc));
        }
    }

    public Function<T> propagate(String keyReceived, String keyToPropagate, Function target) {
        propagate(keyReceived, keyToPropagate, target, propagations);
        return this;
    }

    void propagate(Message m) {
        propagate(m, Acknowledge.N, propagations);
    }

    void propagate(Message m, Acknowledge ack) {
        propagate(m, ack, propagations);
    }

    void propagate(Message message, Map<String, List<Tuple<String, Function<?>>>> propagations) {
        propagate(message, Acknowledge.N, propagations);
    }

    void propagate(Message message, Acknowledge ack, Map<String, List<Tuple<String, Function<?>>>> propagations) {
        List<Tuple<String, Function<?>>> targets = propagations.get(message.key);
        if (targets != null)
        {
            targets.forEach(t -> {
                Message m = message(t.left, message.value, message.origin.sender(this)).ack(ack);
                t.right.receive(m);
            });
        }
    }

    @Override
    protected void process(Message m) {
        trace(m);
        if (!(this instanceof FunctionSignature) && (returnTo == null || returnKey == null)) {
            throw new IllegalStateException("return target not defined in " + this.getClass().getSimpleName());
        }

        if(m.hasKey(Name.removeState))
        {
            removeState(m.origin);
            return;
        }

        State state = getState(m.origin);
        if (m.key.equals(Name.kickOff)) {
            return;// initializeComputation happens in getState() on new state
        }

        if (!isParameter(m.key)) {
            propagate(m);
            return;
        }
        if (m.key.equals(Name.result)) {
            hdlOnReturns(m.origin, onReturnReceived);
        }
        processInner(m, state);
    }

    protected abstract boolean isParameter(String key);

    protected abstract void processInner(Message m, State state);

    public Function<T> constant(String key, Object value) {
        consts.put(key, value);
        return this;
    }

    Object getValue(Message m, String key) {
        if (m.hasKey(key)) {
            return m.value;
        }
        return null;
    }

    protected void returnResult(T result, Origin origin) {
        hdlOnReturns(origin, onReturn);
        returnTo.receive(message(returnKey, result, origin));
    }

    void hdlOnReturns(Origin origin, List<OnReturnForwardItem> items) {
        items.forEach(v -> v.target().receive(message(v.keyValuePair().key(), v.keyValuePair().value(), origin)));
    }

    static boolean computed(Object value) {
        return value != null;
    }

    static boolean computed(Object... value) {
        for (Object o : value) {
            if (o == null)
                return false;
        }
        return true;
    }

    static boolean isTrue(Boolean decision) {
        return decision != null && decision;
    }

    static boolean isFalse(Boolean decision) {
        return decision != null && !decision;
    }

    public static java.util.function.Function<Object, Integer> intConverter = o -> (Integer) o;
    public static java.util.function.Function<Object, Boolean> boolConverter = o -> (Boolean) o;
    public static java.util.function.Function<Object, List<Integer>> listConverter = o -> (List<Integer>) o;
}
