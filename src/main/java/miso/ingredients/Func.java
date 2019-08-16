package miso.ingredients;

import miso.Actress;
import miso.message.Message;
import miso.message.Name;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Func<T> extends Actress {
    Func<?> returnTo;
    String returnKey;

    private Set<String> keysExpected;

    private Map<Source, State> states = new HashMap<>();
    private Map<String, Object> consts = new HashMap<>();

    void removeState(Source source) {
        states.remove(source);
    }

    abstract State newState(Source source);

    abstract List<String> keysExpected();

    /*
     *   propagation data structure:
     *
     *   Map<targetFunc, Map<keyReceived, keyToPropagate to targetFunc>>
     *
     *   all targetFunc know the keyValue used for returning results
     *
     * */

    Map<Func<?>, Map<String, String>> propagations = new HashMap<>();
    List<Func> pings = new ArrayList<>();

    State getState(Source source) {
        State result = states.get(source);
        if (result == null) {
            result = newState(source);
            states.put(source, result);
            fowardConsts(source, consts);
            fowardPings(source, pings);
        }
        result.lastRequested = LocalDateTime.now();
        return result;
    }

    private void fowardConsts(Source source, Map<String, Object> consts) {
        consts.forEach((key, value) -> recieve(new Message(key, value, source)));
    }

    public void addPing(Func target) {
        pings.add(target);
    }

    private void fowardPings(Source source, List<Func> pings) {
        pings.forEach(p -> p.recieve(new Message(Name.ping, null, source)));
    }

    public Func<T> returnTo(String returnKey, Func<?> f) {
        returnTo = f;
        this.returnKey = returnKey;
        return this;
    }

    Set<String> propagationKeysExpected() {
        if (keysExpected == null) {
            keysExpected = propagations.values().stream().flatMap(keyMap -> keyMap.keySet().stream()).collect(Collectors.toSet());
        }
        return keysExpected;
    }

    public void addPropagation(String keyReceived, Func target) {
        propagations.computeIfAbsent(target, k -> new HashMap<>()).put(keyReceived, keyReceived);
    }

    public void addPropagation(String keyReceived, String keyToPropagate, Func target) {
        propagations.computeIfAbsent(target, k -> new HashMap<>()).put(keyReceived, keyToPropagate);
    }

    protected void propagate(Message m, Map<String, String> keyMap, Func target) {
        String key = keyMap.get(m.key);

        if (key != null) {
            target.recieve(newMsg(key, m.value, Source.opId(this, m.source.executionId, m.source.callLevel + 1, returnKey)));
        }
    }

    protected void propagate(Message m){
        propagations.entrySet().stream()
        .filter(e -> e.getValue().values().contains(m.key));

    }

    public void addConst(String key, Object value) {
        consts.put(key, value);
    }

    protected Optional<Object> getValue(Message m, String key) {
        if (m.hasKey(key)) {
            return Optional.of(m.value);
        } else {
            return getConstValue(key);
        }
    }

    public Optional<Object> getConstValue(String key){
        return Optional.ofNullable(consts.get(key));
    }


    void returnResult(T result, Source source) {
        returnTo.recieve(newMsg(returnKey, result, source));
    }

    private Message newMsg(String key, Object value, Source source) {
        return new Message(key, value, source);
    }

    protected boolean isTrue(Boolean b) {
        return b != null && b;
    }

    protected boolean isFalse(Boolean b) {
        return b != null && !b;
    }

    protected boolean computed(Object value) {
        return value != null;
    }
}
