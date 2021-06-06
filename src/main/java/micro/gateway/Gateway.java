package micro.gateway;

import micro.*;
import micro.event.ExEvent;
import micro.Err;
import micro.Name;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Gateway<T> implements _Ex {

    private final F f;
    private final Class<T> resultType;
    private final Env env;
    private final Long id;
    private final boolean recovered;
    private Consumer<T> consumer;

    private final Map<String, T> params = new HashMap<>();
    private Value result;
    private _Ex ex;

    private Gateway(Long id, boolean recovered, Class<T> resultType, F f, Env env) {
        this.f = f;
        this.resultType = resultType;
        this.env = env;
        this.id = id;
        this.recovered = recovered;
    }

    public static <T> Gateway<T> of(Class<T> resultType, F f, Env env) {
        return new Gateway<>(env.getNextExId(), false, resultType, f, env);
    }

    public static <T> Gateway<T> of(long runId, Class<T> resultType, F f, Env env) {
        return new Gateway<>(runId, true, resultType, f, env);
    }

    public Gateway<T> param(String key, T value) {
        params.put(key, value);
        return this;
    }

    public void callAsync(Consumer<T> consumer) {
        this.consumer = consumer;
        startOff();
    }

    @SuppressWarnings("unchecked")
    public T call() {
        startOff();

        while (true) {
            if (getResult() == null) {
                Concurrent.sleep(100);
                continue;
            }

            if (getResult().getName().equals(Names.error)) {
                Err e = (Err) getResult().get();
                throw new RuntimeException(e.exception);
            } else {
                if (!(resultType.isAssignableFrom(getResult().get().getClass()))) {
                    throw new RuntimeException("inconsistent result type");
                }
                return (T) getResult().get();
            }

        }

    }

    private void startOff() {
        if (recovered) {
            env.relatchExecution(f, this);
        } else {
            ex = env.createExecution(f, this);
            if (params.size() == 0) {
                ex.receive(Value.of(Names.ping, 0, this));
            } else {
                params.forEach((k, v) -> ex.receive(Value.of(k, v, this)));
            }
        }
    }

    @Override
    public _Ex returnTo() {
        throw new RuntimeException("no call expected");
    }

    @Override
    public _F getTemplate() {
        return FGateway.instance;
    }

    @Override
    public void receive(Value v) {
        if (!(resultType.isAssignableFrom(v.get().getClass()))) {
            throw new RuntimeException("inconsistent result type " + v.get().getClass().getSimpleName());
        }
        setResult(v);
        if (consumer != null) {
            //noinspection unchecked
            consumer.accept((T) v.get());
        }
    }

    @Override
    public void recover(ExEvent e) {

    }

    @Override
    public Address getAddress() {
        throw new RuntimeException("no call expected");
    }

    @Override
    public String getLabel() {
        return "Gateway";
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long value) {
        throw new RuntimeException("no call expected");
    }

    @Override
    public String toString() {
        return "Gateway(" + f.getLabel() + "())";
    }

    @Override
    public boolean isMoreToDoRightNow() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true; // doesn't matter here, but false would cause it to linger in crank list
    }

    @Override
    public void crank() {

    }

    private synchronized Value getResult() {
        return result;
    }

    private synchronized void setResult(Value result) {
        this.result = result;
    }
}
