package nano.ingredients.gateway;

import nano.ingredients.*;
import nano.ingredients.Origin;

import java.io.Serializable;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class Gateway<T extends Serializable> extends Function<T> {

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
    public void tell(Message message) {
        Optional<Execution<T>> ex = executions.stream()
                .filter(e -> e.origin.getExecutionId() == message.origin.getExecutionId())
                .findAny();
        if (! ex.isPresent())
        {
            throw new IllegalStateException();
        }

        ex.get().setResult((T) message.getValue());
        executions.remove(ex.get());
    }

    @Override
    protected State newState(Origin origin) {
        throw new IllegalStateException();
    }

    @Override
    protected boolean belongsToMe(String key) {
        throw new IllegalStateException();
    }

    @Override
    protected void processInner(Message m, State state) {
        throw new IllegalStateException();
    }

}
