package miso.ingredients.gateway;

import miso.ingredients.Function;
import miso.ingredients.Source;
import miso.misc.Name;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static miso.ingredients.Message.message;
import static miso.ingredients.Source.source;

public class Execution<T> implements Future<T> {
    private static Long maxExecutionId = 0L;

    private final Function<T> f;
    private T result;
    private final Consumer<T> onFinished;
    final Source source;

    public void setResult(T result) {
        this.result = result;
        if (onFinished != null) {
            onFinished.accept(result);
        }
    }

    public Execution(Function<T> f, Consumer<T> onFinished) {
        this.f = f;
        this.onFinished = onFinished;
        source = source(f, getNextExecutionId(), 0);
    }

    private Long getNextExecutionId() {
        synchronized (maxExecutionId) {
            return maxExecutionId++;
        }
    }

    public Execution<T> param(String key, Integer value) {
        f.recieve(message(key, value, source));
        return this;
    }

    private Execution<T> kickOff() {
        f.recieve(message(Name.kickOff, null, source));
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
