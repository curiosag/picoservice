package miso.ingredients;

import java.util.HashMap;
import java.util.Map;

public class KeyTranslator<T> extends Function<T> {

    private final Function<T> inner;
    private Map<String, String> keyMap = new HashMap<>();

    private KeyTranslator(Function<T> inner) {
        this.inner = inner;
    }

    public static <T> KeyTranslator<T> keyMapper(Function<T> inner){
        return new KeyTranslator<>(inner);
    }

    public KeyTranslator<T> map(String from, String to) {
        this.debug = true;
        keyMap.put(from, to);
        return this;
    }

    @Override
    public void receive(Message m) {
        debug(String.format("<%d> try to map key %s", m.origin.seqNr, m.key));
        String mapped = keyMap.get(m.key);
        if (mapped != null) {
            inner.receive(m.usingKey(mapped));
        } else {
            inner.receive(m);
        }
    }

    @Override
    protected State newState(Origin origin) {
        throw new IllegalStateException();
    }

    @Override
    protected boolean isParameter(String key) {
        throw new IllegalStateException();
    }

    @Override
    protected void processInner(Message m, State state) {
        throw new IllegalStateException();
    }
}
