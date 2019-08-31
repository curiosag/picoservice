package miso.ingredients;

import java.util.HashMap;
import java.util.Map;

import static miso.ingredients.Message.message;
import static miso.ingredients.Nop.nop;
import static miso.ingredients.Origin.origin;

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
    synchronized public void receive(Message message) {
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
