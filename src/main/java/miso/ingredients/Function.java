package miso.ingredients;

import miso.misc.Name;

import java.util.*;
import java.util.Map.Entry;

import static miso.ingredients.Message.message;

public abstract class Function<T> extends Actress {
    Function<?> returnTo;
    String returnKey;

    // Tuple<ExecutionId, CallLevel> -> State
    public final Map<Tuple<Long, Integer>, State> executionStates = new HashMap<>();
    private final Map<String, Object> consts = new HashMap<>();

    private void removeState(Source source) {
        executionStates.remove(new Tuple<>(source.executionId, source.callLevel));
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
        return executionStates.get(callId);
    }

    private final Tuple execution = Tuple.of(0L, 0);

    State getState(Source source) {
        execution.left = source.executionId;
        execution.right = source.callLevel;
        State result = getState(execution);
        if (result == null) {
            result = newState(source);
            executionStates.put(Tuple.of(source.executionId, source.callLevel), result);
            fowardConsts(source, consts);
            forwardDependent(source, dependent);
        }
        result.lastRequested = System.nanoTime();
        return result;
    }

    protected void fowardConsts(Source source, Map<String, Object> consts) {
        consts.forEach((key, value) -> recieve(message(key, value, source.withHost(this))));
    }

    public Function<T> kickOff(Function target) {
        dependent.add(target);
        return this;
    }

    protected void forwardDependent(Source source, List<Function> dependent) {
        dependent.forEach(p -> p.recieve(message(Name.kickOff, null, source)));
    }

    protected void finalizeDependent(Source source, List<Function> dependent) {
        dependent.forEach(p -> p.recieve(message(Name.finalizeComputation, null, source)));
    }

    public Function<T> returnTo(Function<?> f, String returnKey) {
        returnTo = f;
        this.returnKey = returnKey;
        return this;
    }

    protected Function<T> propagate(String keyReceived, String keyToPropagate, Function target, Map<Function<?>, Map<String, List<String>>> propagations) {
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

    public Function<T> propagate(String keyReceived, String keyToPropagate, Function target) {
        propagate(keyReceived, keyToPropagate, target, propagations);
        return this;
    }

    protected void propagate(Message m) {
        propagate(m, propagations);
    }

    protected void propagate(Message m, Map<Function<?>, Map<String, List<String>>> propagations) {
        for (Entry<Function<?>, Map<String, List<String>>> prop : propagations.entrySet()) {
            for (Entry<String, List<String>> keyMapping : prop.getValue().entrySet()) {
                if (keyMapping.getKey().equals(m.key)) {
                    keyMapping.getValue().forEach(targetName -> prop.getKey().recieve(message(targetName, m.value, m.source.withHost(this))));
                }
            }
        }
    }

    @Override
    protected void process(Message m) {
        State state = getState(m.source);
        if (m.key.equals(Name.kickOff)) {
            return;// initializeComputation happens in getState() on new state
        }

        if (m.key.equals(Name.finalizeComputation)) {
            removeState(m.source);
            finalizeDependent(state.source, dependent);
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

    void returnResult(T result, Source source) {
        returnTo.recieve(newMsg(returnKey, result, source));
    }

    private Message newMsg(String key, Object value, Source source) {
        return message(key, value, source);
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
