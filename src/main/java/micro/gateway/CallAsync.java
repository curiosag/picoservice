package micro.gateway;

import micro.*;
import micro.event.ExEvent;
import nano.ingredients.Name;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CallAsync<T extends Serializable> implements _Ex {

    private final F f;
    private final Class<T> resultType;
    Consumer<T> consumer;
    private final Env env;

    private final Map<String, Integer> params = new HashMap<>();

    private CallAsync(Class<T> resultType, F f, Consumer<T> consumer, Env env) {
        this.f = f;
        this.resultType = resultType;
        this.consumer = consumer;
        this.env = env;
    }

    public static <T extends Serializable> CallAsync<T> of(Class<T> resultType, F f, Consumer<T> consumer, Env env) {
        return new CallAsync<>(resultType, f, consumer, env);
    }

    public CallAsync<T> param(String key, Integer value) {
        params.put(key, value);
        return this;
    }

    public void call() {
        call(null);
    }

    public void call(Integer relatchToId) {
        _Ex ex = env.createExecution(f,this);

        if (params.size() == 0) {
            ex.receive(Value.of(Name.kickOff, 0, this));
        } else {
            params.forEach((k, v) -> ex.receive(Value.of(k, v, this)));
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

    @SuppressWarnings("unchecked")
    @Override
    public void receive(Value v) {
        if (!(resultType.isAssignableFrom(v.get().getClass()))) {
            throw new RuntimeException("inconsistent result type " + v.get().getClass().getSimpleName());
        }
        consumer.accept((T) v.get());
    }

    @Override
    public void recover(ExEvent e) {

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
        return "AsyncCall{f:" + f.getLabel() + "}";
    }

    @Override
    public boolean isMoreToDoRightNow() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public void crank() {

    }

    @Override
    public String getLabel() {
        return "CallSync";
    }
}
