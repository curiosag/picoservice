package micro.gateway;

import micro.*;
import micro.event.ExEvent;
import nano.ingredients.Err;
import nano.ingredients.Name;

import java.util.HashMap;
import java.util.Map;

public class CallSync<T> implements _Ex {

    private final F f;
    private final Class<T> resultType;
    private final Node node;
    private Long latchOntoExId;

    private final Map<String, T> params = new HashMap<>();
    private Value result;
    private _Ex ex;

    private CallSync(Class<T> resultType, F f, Node node) {
        this(null, resultType, f, node);
    }

    private CallSync(Long latchOntoExId, Class<T> resultType, F f, Node node) {
        this.f = f;
        this.resultType = resultType;
        this.node = node;
        this.latchOntoExId = latchOntoExId;
    }

    public static <T> CallSync<T> of(Class<T> resultType, F f, Node node) {
        return new CallSync<>(resultType, f, node);
    }

    public CallSync<T> param(String key, T value) {
        params.put(key, value);
        return this;
    }

    public CallSync<T> latchOnto(Long exId)
    {
        this.latchOntoExId = exId;
        return this;
    }

    public long prepareEx() {
        ex = node.createExecution(f, this);
        return ex.getId();
    }

    @SuppressWarnings("unchecked")
    public T call() {
        if(latchOntoExId == null) {
            if (ex == null)
                prepareEx();
            if (params.size() == 0) {
                ex.receive(Value.of(Name.kickOff, 0, this));
            } else {
                params.forEach((k, v) -> ex.receive(Value.of(k, v, this)));
            }
        } else {
            node.relatchExecution(latchOntoExId, f, this);
        }

        while (true) {
            if (getResult() == null) {
                Concurrent.sleep(100);
                continue;
            }

            if (getResult().getName().equals(Name.error)) {
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
        setResult(v);
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
        return "CallSync";
    }

    @Override
    public long getId() {
        return -1;
    } //TODO won't work for multiple sync calls simultaneously

    @Override
    public void setId(long value) {
        throw new RuntimeException("no call expected");
    }

    @Override
    public String toString() {
        return "SyncCall{f:" + f.getLabel() + "}";
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

    private synchronized Value getResult() {
        return result;
    }

    private synchronized void setResult(Value result) {
        this.result = result;
    }
}
