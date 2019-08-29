package miso.ingredients.gateway;

import miso.ingredients.Function;
import miso.ingredients.Message;
import miso.ingredients.Source;
import miso.ingredients.State;
import miso.misc.Name;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Gateway<T> extends Function<T> {

    private final Map<Long, Execution<T>> executions = new HashMap<>();

    public static Gateway<Integer> intGateway() {
        return new Gateway<>();
    }

    public Execution<T> execute(Function<T> f) {
        f.returnTo(this, Name.result);
        Execution<T> e = new Execution<T>(f, x -> {
        });
        synchronized (executions) {
            executions.put(e.source.executionId, e);
        }
        return e;
    }

    public Execution<T> execute(Function<T> f, Consumer<T> onFinished) {
        f.returnTo(this, Name.result);
        Execution<T> e = new Execution<T>(f, onFinished);
        executions.put(e.source.executionId, e);
        return e;
    }

    @Override
    synchronized public void recieve(Message message) {
        Execution<T> e = executions.get(message.source.executionId);
        e.setResult((T) message.value);
        synchronized (executions) {
            executions.remove(e.source.executionId);
        }
    }

    @Override
    public void run() {
        throw new IllegalStateException();
    }

    @Override
    protected State newState(Source source) {
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
