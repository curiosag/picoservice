package miso.ingredients.gateway;

import miso.ingredients.Function;
import miso.ingredients.Message;
import miso.ingredients.Source;
import miso.ingredients.State;
import miso.misc.Name;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class Gateway<T> extends Function<T> {

    private Queue<Execution<T>> executions = new ConcurrentLinkedQueue<>();

    public static Gateway<Integer> intGateway() {
        return new Gateway<>();
    }

    public Execution<T> execute(Function<T> f) {
        f.returnTo(this, Name.result);
        Execution<T> e = new Execution<T>(f, x -> {
        });
        executions.add(e);
        return e;
    }

    public Execution<T> execute(Function<T> f, Consumer<T> onFinished) {
        f.returnTo(this, Name.result);
        Execution<T> e = new Execution<T>(f, onFinished);
        executions.add(e);
        return e;
    }

    @Override
    public void recieve(Message message) {
        Optional<Execution<T>> ex = executions.stream()
                .filter(e -> e.source.executionId.equals(message.source.executionId))
                .findAny();
        if (! ex.isPresent())
        {
            throw new IllegalStateException();
        }

        ex.get().setResult((T) message.value);
        executions.remove(ex.get());
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
