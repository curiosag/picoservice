package nano.ingredients;

import nano.ingredients.tuples.ForwardingItem;
import nano.ingredients.tuples.SerializableKeyValuePair;
import nano.ingredients.tuples.SerializableTuple;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static nano.ingredients.Message.message;

public abstract class Function<T extends Serializable> extends Actress {

    public Function<?> returnTo;
    public String returnKey = Name.result;
    final List<ForwardingItem> onReturn = new ArrayList<>();
    private final List<ForwardingItem> onReceivedReturn = new ArrayList<>();

    public final Map<ComputationPathLocation, FunctionState> executionStates = new HashMap<>();
    private final Map<String, Serializable> consts = new HashMap<>();

    public Function(Address address) {
        super(address);
    }

    public Function() {
        super();
    }

    protected void removeState(Origin origin) {
        executionStates.remove(origin.computationPathLocation());
    }

    protected boolean isConst(String key){
        return consts.keySet().contains(key);
    }

    void cleanup(Long runId) {
        // TODO implement
        List<ComputationPathLocation> toRemove = executionStates.keySet().stream()
                .filter(k -> k.getExecutionId().equals(runId))
                .collect(Collectors.toList());
        toRemove.forEach(executionStates::remove);
    }

    public void onReturnSend(String key, Serializable value, Function<?> target) {
        onReturn.add(ForwardingItem.of(SerializableKeyValuePair.of(key, value), target));
    }

    public void onReceivedReturnSend(String key, Serializable value, Function target) {
        onReceivedReturn.add(ForwardingItem.of(SerializableKeyValuePair.of(key, value), target));
    }

    @Override
    public void checkSanityOnStop() {
        super.checkSanityOnStop();
        if (!executionStates.isEmpty()) {
            System.out.println(String.format("-->  %s %s: %d execution states left after stop", address.toString(), address.toString(), executionStates.size()));
            executionStates.clear();
        }
    }

    protected abstract FunctionState newState(Origin origin);


    /*
     *   propagation data structure:
     *
     *   Map<keyReceived , List<Tuple<List<keysToPropagate>,targetFunc>>
     *
     *   all targetFunc know the keyValue used for returning results
     *
     * */
    private final Map<String, List<SerializableTuple<String, Function<?>>>> propagations = new HashMap<>();

    private List<Function> kicks = new ArrayList<>();

    /*
    * getState(Origin origin)
    *
    * considers only computationPathLocation from origin
    * */
    FunctionState getState(Origin origin) {
        FunctionState result = executionStates.get(origin.computationPathLocation());
        if (result == null) {
            result = newState(origin);
            executionStates.put(origin.computationPathLocation(), result);
            fowardConsts(origin);
            forwardKickOff(origin);
        }
        return result;
    }

    private void fowardConsts(Origin origin) {
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

    void propagate(String keyReceived, String keyToPropagate, Function targetFunc, Map<String, List<SerializableTuple<String, Function<?>>>> propagations) {
        List<SerializableTuple<String, Function<?>>> targets = propagations.computeIfAbsent(keyReceived, k -> new ArrayList<>());

        if (targets.stream().noneMatch(i -> i.left.equals(keyToPropagate) && i.right.equals(targetFunc))) {
            targets.add(new SerializableTuple<String, Function<?>>(keyToPropagate, targetFunc));
        }
    }

    public Function<T> propagate(String keyReceived, String keyToPropagate, Function target) {
        propagate(keyReceived, keyToPropagate, target, propagations);
        return this;
    }

    void propagate(Message m) {
        propagate(m, Acknowledge.N, propagations);
    }

    void propagateAck(Message m) {
        propagate(m, Acknowledge.Y, propagations);
    }

    void propagate(Message message, Map<String, List<SerializableTuple<String, Function<?>>>> propagations) {
        propagate(message, Acknowledge.N, propagations);
    }

    private void propagate(Message message, Acknowledge ack, Map<String, List<SerializableTuple<String, Function<?>>>> propagations) {
        List<SerializableTuple<String, Function<?>>> targets = propagations.get(message.key);
        if (targets != null) {
            targets.forEach(t -> {
                Message m = message(t.left, message.getValue(), message.origin.sender(this)).ack(ack);
                t.right.tell(m);
            });
        }
    }

    @Override
    public void process(Message m) {

        trace(m);
        if (!((this instanceof FunctionSignature) || (this instanceof PartialFunctionApplication)) &&
                (returnTo == null || returnKey == null)) {
            throw new IllegalStateException("return target not defined in " + this.getClass().getSimpleName());
        }

        if (m.hasKey(Name.removeState)) {
            removeState(m.origin);
            return;
        }

        FunctionState state = getState(m.origin);
        if (m.key.equals(Name.kickOff)) {
            return;// initializeComputation happens in getState() on new state
        }

        if (shouldPropagate(m.key)) {
            propagate(m);
            return;
        }
        if (m.key.equals(Name.result)) {
            hdlForwardings(m.origin, onReceivedReturn);
        }

        try {
            processInner(m, state);
        } catch (Exception e) {
            debug(getExceptionMessage(m, e));
            returnTo.aRef().tell(errMessage(m, e), aRef());
        }
    }

    private Message errMessage(Message m, Exception e) {
        return message(Name.error, new Err(this, m, e), m.origin.sender(this));
    }

    protected abstract boolean shouldPropagate(String key);

    protected abstract void processInner(Message m, FunctionState state);

    public Function<T> constant(String key, Serializable value) {
        consts.put(key, value);
        return this;
    }

    Object getValue(Message m, String key) {
        if (m.hasKey(key)) {
            return m.getValue();
        }
        return null;
    }

    protected void returnResult(T result, Origin origin) {
        hdlForwardings(origin, onReturn);
        returnTo.tell(message(returnKey, result, origin));
    }

    void hdlForwardings(Origin origin, List<ForwardingItem> items) {
        items.forEach(v -> {
            SerializableKeyValuePair kv = v.keyValuePair();
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

    public static boolean in(String value, String ... values)
    {
        if (values == null)
        {
            throw new IllegalStateException();
        }
        return Arrays.asList(values).contains(value);
    }

    static boolean isTrue(Boolean decision) {
        return decision != null && decision;
    }

    static boolean isFalse(Boolean decision) {
        return decision != null && !decision;
    }

    public static java.util.function.Function<Object, Integer> intConverter = (java.util.function.Function<Object, Integer> & Serializable) o -> (Integer) o;
    public static java.util.function.Function<Object, Boolean> boolConverter = (java.util.function.Function<Object, Boolean> & Serializable) o -> (Boolean) o;
    public static java.util.function.Function<Object, ArrayList<Integer>> listConverter = (java.util.function.Function<Object, ArrayList<Integer>> & Serializable) o -> (ArrayList<Integer>) o;
}