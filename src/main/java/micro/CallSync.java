package micro;

import nano.ingredients.Err;
import nano.ingredients.Name;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CallSync<T extends Serializable> implements _Ex {

    private final F f;
    private final Class<T> resultType;

    private final Map<String, Integer> params = new HashMap<>();
    private Value result;

    private CallSync(Class<T> resultType, F f) {
        this.f = f;
        this.resultType = resultType;
    }

    public static <T extends Serializable> CallSync<T> of(Class<T> resultType, F f) {
        return new CallSync<>(resultType, f);
    }

    public CallSync<T> param(String key, Integer value) {
        params.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public T call() {
        _Ex ex = f.createExecution(this);

        if (params.size() == 0) {
            ex.receive(Value.of(Name.kickOff, 0, this));
        } else {
            params.forEach((k, v) -> ex.receive(Value.of(k, v, this)));
        }

        while (true) {
            if (result == null) {
                Concurrent.sleep(10);
                continue;
            }

            if (result.getName().equals(Name.error)) {
                Err e = (Err) result.get();
                throw new RuntimeException(e.exception);
            } else {
                if (!(resultType.isAssignableFrom(result.get().getClass()))) {
                    throw new RuntimeException("inconsistent result type");
                }
                return (T) result.get();
            }

        }

    }

    @Override
    public _Ex returnTo() {
        throw new RuntimeException("no call expected");
    }

    @Override
    public _F getTemplate() {
        throw new RuntimeException("no call expected");
    }

    @Override
    public void receive(Value v) {
        result = v;
    }

    @Override
    public Address getAddress() {
        throw new RuntimeException("no call expected");
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public void setId(long value) {
        throw new RuntimeException("no call expected");
    }

    @Override
    public String toString() {
        return "SyncCall{f:" + f.getLabel() + "}";
    }
}
