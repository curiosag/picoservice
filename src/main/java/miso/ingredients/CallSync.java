package miso.ingredients;

import miso.misc.Name;

import java.util.HashMap;
import java.util.Map;

import static miso.ingredients.Message.message;
import static miso.ingredients.Source.source;

public class CallSync extends Function<Integer> {

    private final Function<Integer> f;
    private Integer result;
    private static Long executions = 0L;

    private Map<String, Integer> params = new HashMap<>();

    private CallSync(Function<Integer> f) {
        f.returnTo(this, Name.result);
        this.f = f;
    }

    public static CallSync sync(Function<Integer> f) {
        return new CallSync(f);
    }

    public CallSync param(String key, Integer value) {
        params.put(key, value);
        return this;
    }

    public Integer call() {
        Source source = source(f, executions++, 0);
        if (params.size() == 0) {
            f.recieve(message(Name.initializeComputation, null, source));
        } else {
            params.forEach((k, v) -> f.recieve(message(k, v, source)));
        }

        while (true) {
            if (result != null) {
                f.recieve(message(Name.finalizeComputation, null, source));
                return result;
            }
            waitSome(100L);
        }
    }

    private void waitSome(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    synchronized public void recieve(Message message) {
        if (message.value == null || !message.hasKey(Name.result) || !(message.value instanceof Integer)) {
            throw new IllegalStateException();
        }
        result = (Integer) message.value;
    }

    @Override
    public void run() {
        throw new IllegalArgumentException();
    }

    @Override
    State newState(Source source) {
        throw new IllegalArgumentException();
    }

    @Override
    protected boolean isParameter(String key) {
        return !key.equals(Name.result);
    }

    @Override
    protected void processInner(Message m, State state) {
        throw new IllegalStateException();
    }

}
