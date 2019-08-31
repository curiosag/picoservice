package miso.ingredients;

import java.util.HashMap;
import java.util.Map;

import static miso.ingredients.Message.message;
import static miso.ingredients.Nop.nop;

public class CallSync<T> extends Function<T> {

    private final Function<T> f;
    private T result;
    private static Long executions = 0L;

    private Map<String, Integer> params = new HashMap<>();

    private CallSync(Function<T> f) {
        f.returnTo(this, Name.result);
        this.f = f;
    }

    public static <T> CallSync<T> sync(Function<T> f) {
        return new CallSync<>(f);
    }

    public CallSync param(String key, Integer value) {
        params.put(key, value);
        return this;
    }

    public T call() {
        Origin origin = Origin.origin(f, nop, executions++, 0);
        if (params.size() == 0) {
            f.receive(message(Name.kickOff, null, origin));
        } else {
            params.forEach((k, v) -> f.receive(message(k, v, origin)));
        }

        while (true) {
            if (result != null) {
                return result;
            }
            waitSome();
        }
    }

    private void waitSome() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    synchronized public void receive(Message message) {
        if (message.value == null || !message.hasKey(Name.result)) {
            throw new IllegalStateException();
        }
        result = (T) message.value;
    }

    @Override
    public void run() {
        throw new IllegalArgumentException();
    }

    @Override
    protected State newState(Origin origin) {
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
