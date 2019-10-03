package nano.ingredients;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static nano.ingredients.Message.message;

public class CallSync<T extends Serializable> extends Function<T> {

    private final Function<T> f;
    private Message resultMessage;
    private static Long executions = 0L;

    private Map<String, Integer> params = new HashMap<>();

    private CallSync(Function<T> f) {
        f.returnTo(this, Name.result);
        this.f = f;
    }

    public static <T extends Serializable> CallSync<T> sync(Function<T> f) {
        return new CallSync<>(f);
    }

    public CallSync param(String key, Integer value) {
        params.put(key, value);
        return this;
    }

    public T call() {
        resultMessage = null;
        Origin origin = Origin.origin(this, executions++,  new ComputationBough(), -1L, 0L);
        if (params.size() == 0) {
            f.tell(message(Name.kickOff, null, origin));
        } else {
            params.forEach((k, v) -> f.tell(message(k, v, origin)));
        }

        while (true) {
            if (resultMessage != null) {
                if (resultMessage.key.equals(Name.error)) {
                    Err e = (Err) resultMessage.getValue();
                    throw new RuntimeException(e.exception);
                } else {
                    return (T) resultMessage.getValue();
                }
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
    synchronized public void tell(Message message) {
        if (message.getValue() == null || !message.hasKey(Name.result)) {
            throw new IllegalStateException();
        }
        resultMessage = message;
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
