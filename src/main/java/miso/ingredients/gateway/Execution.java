package miso.ingredients.gateway;

import miso.ingredients.Function;
import miso.ingredients.Origin;
import miso.ingredients.Name;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static miso.ingredients.Message.message;
import static miso.ingredients.Nop.nop;
import static miso.ingredients.Origin.origin;

public class Execution<T> implements Future<T> {
    private static AtomicLong maxExecutionId = new AtomicLong(0);

    private final Function<T> f;
    private T result;
    private final Consumer<T> onFinished;
    final Origin origin;

    public void setResult(T result) {
        this.result = result;
        if (onFinished != null) {
            onFinished.accept(result);
        }
    }

    public Execution(Function<T> f, Consumer<T> onFinished) {
        this.f = f;
        this.onFinished = onFinished;
        origin = Origin.origin(f, nop, maxExecutionId.addAndGet(1), 0);
    }

    public Execution<T> param(String key, Integer value) {
        f.receive(message(key, value, origin));
        return this;
    }

    private Execution<T> kickOff() {
        f.receive(message(Name.kickOff, null, origin));
        return this;
    }

    @Override
    public boolean cancel(boolean b) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return result != null;
    }

    @Override
    public T get() {
        return awaitResult();
    }

    @Override
    public T get(long l, TimeUnit timeUnit) {
        return null;
    }

    private T awaitResult() {
        while (true) {
            if (isDone()) {
                return result;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                //
            }
        }
    }


}