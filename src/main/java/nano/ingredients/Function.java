package nano.ingredients;

import nano.ingredients.tuples.KeyValuePair;
import nano.ingredients.tuples.ForwardingItem;
import nano.ingredients.tuples.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nano.ingredients.Message.message;

public abstract class Function<T> extends Actress {

    public Function<?> returnTo;
    public String returnKey;
    final List<ForwardingItem> onReturn = new ArrayList<>();
    private final List<ForwardingItem> onReceivedReturn = new ArrayList<>();

    public final Map<FunctionCallTreeLocation, State> executionStates = new HashMap<>();
    private final Map<String, Object> consts = new HashMap<>();

    protected void removeState(Origin origin) {
        executionStates.remove(origin.functionCallTreeNode());
        //debug(String.format("-->  %s:%d States. Removed (%d/%d) %s ", address.toString(), executionStates.size(), origin.executionId, origin.callLevel, origin.sender.address.toString()));
    }

    void cleanup(Long runId) {
        List<FunctionCallTreeLocation> toRemove = executionStates.keySet().stream()
                .filter(k -> k.getExecutionId().equals(runId))
                .collect(Collectors.toList());
        toRemove.forEach(executionStates::remove);
    }

    public void onReturnSend(String key, Object value, Function<?> target) {
        onReturn.add(ForwardingItem.of(KeyValuePair.of(key, value), target));
    }

    public void onReceivedReturnSend(String key, Object value, Function target) {
        onReceivedReturn.add(ForwardingItem.of(KeyValuePair.of(key, value), target));
    }

    @Override
    public void checkSanityOnStop() {
        super.checkSanityOnStop();
        if (!executionStates.isEmpty()) {
            System.out.println(String.format("-->  %s %s: %d execution states left after stop", address.toString(), address.toString(), executionStates.size()));
            executionStates.clear();
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
        State result = executionStates.get(origin.functionCallTreeNode());
        if (result == null) {
            result = newState(origin);
            executionStates.put(origin.functionCallTreeNode(), result);
            fowardConsts(origin);
            forwardKickOff(origin);
        }
        return result;
    }

    protected void fowardConsts(Origin origin) {
        consts.forEach((key, value) -> tell(message(key, value, origin.sender(this))));
    }

    public Function<T> kickOff(Function target) {
        kicks.add(target);
        return this;
    }

    private void forwardKickOff(Origin origin) {
        kicks.forEach(p -> p.tell(message(Name.kickOff, null, origin.sender(this))));
    }

    public Function<T> returnTo(Function<?> f, String returnKey) {
        returnTo = f;
        this.returnKey = returnKey;
        return this;
    }

    void propagate(String keyReceived, String keyToPropagate, Function targetFunc, Map<String, List<Tuple<String, Function<?>>>> propagations) {
        List<Tuple<String, Function<?>>> targets = propagations.computeIfAbsent(keyReceived, k -> new ArrayList<>());

        if (targets.stream().noneMatch(i -> i.left.equals(keyToPropagate) && i.right.equals(targetFunc))) {
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
        if (targets != null) {
            targets.forEach(t -> {
                Message m = message(t.left, message.value, message.origin.sender(this)).ack(ack);
                t.right.tell(m);
            });
        }
    }

    @Override
    public void process(Message m) {

        trace(m);
        if (!(this instanceof FunctionSignature) && (returnTo == null || returnKey == null)) {
            throw new IllegalStateException("return target not defined in " + this.getClass().getSimpleName());
        }

        if (m.hasKey(Name.removeState)) {
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
            hdlForwarings(m.origin, onReceivedReturn);
        }

        try {
            processInner(m, state);
        } catch (Exception e) {
            debug(getExceptionMessage(m, e));
            returnTo.aRef().tell(errMessage(m, e), aRef());
        }
    }

    protected Message errMessage(Message m, Exception e) {
        return message(Name.error, new Err(this, m, e), m.origin.sender(this));
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
        hdlForwarings(origin, onReturn);
        returnTo.tell(message(returnKey, result, origin));
    }

    void hdlForwarings(Origin origin, List<ForwardingItem> items) {
        items.forEach(v -> {
            KeyValuePair kv = v.keyValuePair();
            v.target().tell(message(kv.key(), kv.value(), origin));
        });
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
