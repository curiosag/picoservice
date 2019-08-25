package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.HashMap;
import java.util.Map;

public class CallSync extends Function<Integer> {

    private final Function<Integer> f;
    private Integer result;
    private static Long executions = 0L;

    private Map<String, Integer> params = new HashMap<>();

    private CallSync(Function<Integer> f) {
        f.returnTo(Name.result, this);
        this.f = f;
    }

    public static CallSync callSync(Function<Integer> f) {
        return new CallSync(f);
    }

    public CallSync param(String key, Integer value) {
        params.put(key, value);
        return this;
    }

    public Integer call() {
        Source source = new Source(f, executions++, 0);
        if (params.size() == 0) {
            f.recieve(new Message(Name.initializeComputation, null, source));
        } else {
            params.forEach((k, v) -> f.recieve(new Message(k, v, source)));
        }

        while (true) {
            if (result != null) {
                f.recieve(new Message(Name.finalizeComputation, null, source));
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
