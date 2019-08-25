package miso.ingredients;

import miso.Actress;
import miso.message.Message;
import miso.message.Name;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;

public abstract class Function<T> extends Actress {
    Function<?> returnTo;
    String returnKey;

    // Tuple<ExecutionId, CallLevel> -> State
    public final Map<Tuple<Long, Integer>, State> states = new HashMap<>();
    private final Map<String, Object> consts = new HashMap<>();

    private void removeState(Source source) {
        states.remove(new Tuple<>(source.executionId, source.callLevel));
    }

    abstract State newState(Source source);

    /*
     *   propagation data structure:
     *
     *   Map<targetFunc, Map<keyReceived, List<keysToPropagate to targetFunc>>>
     *
     *   all targetFunc know the keyValue used for returning results
     *
     * */

    Map<Function<?>, Map<String, List<String>>> propagations = new HashMap<>();

    private List<Function> dependent = new ArrayList<>();

    private State getState(Tuple<Long, Integer> callId) {
        return states.get(callId);
    }

    State getState(Source source) {
        Tuple<Long, Integer> OpId = Tuple.of(source.executionId, source.callLevel);
        State result = getState(OpId);
        if (result == null) {
            result = newState(source);
            states.put(Tuple.of(source.executionId, source.callLevel), result);
            fowardConsts(source, consts);
            initializeDependent(source, dependent);
        }
        result.lastRequested = LocalDateTime.now();
        return result;
    }

    protected void fowardConsts(Source source, Map<String, Object> consts) {
        consts.forEach((key, value) -> recieve(new Message(key, value, source.withHost(this))));
    }

    public Function<T> addInitAndFinalize(Function target) {
        dependent.add(target);
        return this;
    }

    protected void initializeDependent(Source source, List<Function> dependent) {
        dependent.forEach(p -> p.recieve(new Message(Name.initializeComputation, null, source)));
    }

    protected void terminateDependent(Source source, List<Function> dependent) {
        dependent.forEach(p -> p.recieve(new Message(Name.finalizeComputation, null, source)));
    }

    public Function<T> returnTo(String returnKey, Function<?> f) {
        returnTo = f;
        this.returnKey = returnKey;
        return this;
    }

    protected Function<T> addPropagation(String keyReceived, String keyToPropagate, Function target, Map<Function<?>, Map<String, List<String>>> propagations) {
        Map<String, List<String>> prop = propagations.get(target);
        if (prop == null) {
            propagations.put(target, new HashMap<>());
            prop = propagations.get(target);
        }

        List<String> targets = prop.get(keyReceived);
        if (targets == null) {
            prop.put(keyReceived, new ArrayList<>());
            targets = prop.get(keyReceived);
        }

        targets.add(keyToPropagate);

        return this;
    }

    public Function<T> addPropagation(String keyReceived, String keyToPropagate, Function target) {
        addPropagation(keyReceived, keyToPropagate, target, propagations);
        return this;
    }

    protected void propagate(Message m) {
        propagate(m, propagations);
    }

    protected void propagate(Message m, Map<Function<?>, Map<String, List<String>>> propagations) {
        for (Entry<Function<?>, Map<String, List<String>>> prop : propagations.entrySet()) {
            for (Entry<String, List<String>> keyMapping : prop.getValue().entrySet()) {
                if (keyMapping.getKey().equals(m.key)) {
                    keyMapping.getValue().forEach(targetName -> prop.getKey().recieve(new Message(targetName, m.value, m.source.withHost(this))));
                }
            }
        }
    }

    @Override
    protected void process(Message m) {
        State state = getState(m.source);
        if (m.key.equals(Name.initializeComputation)) {
            return;// initializeComputation happens in getState() on new state
        }

        if (m.key.equals(Name.finalizeComputation)) {
            removeState(m.source);
            terminateDependent(state.source, dependent);
            return;
        }
        if (!isParameter(m.key)) {
            propagate(m);
            return;
        }
        processInner(m, state);
    }

    protected abstract boolean isParameter(String key);

    protected abstract void processInner(Message m, State state);

    public Function<T> addConst(String key, Object value) {
        consts.put(key, value);
        return this;
    }

    Object getValue(Message m, String key) {
        if (m.hasKey(key)) {
            return m.value;
        }
        return null;
    }

    void returnResult(T result, Source source) {
        returnTo.recieve(newMsg(returnKey, result, source));
    }

    private Message newMsg(String key, Object value, Source source) {
        return new Message(key, value, source);
    }

    boolean computed(Object value) {
        return value != null;
    }

    protected boolean isTrue(Boolean decision) {
        return decision != null && decision;
    }

    protected boolean isFalse(Boolean decision) {
        return decision != null && !decision;
    }
}
