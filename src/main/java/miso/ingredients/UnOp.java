package miso.ingredients;

import miso.Message;
import miso.Name;

import java.util.function.Function;

public class UnOp<T, V> extends Func {

    private final Function<T, V> op;
    private final Function<Object, T> converter;

    public UnOp(Function<T, V> op, Function<Object, T> converter) {
        this.op = op;
        this.converter = converter;
        await(Name.arg);
    }

    @Override
    protected Message getNext() {
        return getCurrent()
                .map(m -> {
                    try {
                        T arg = converter.apply(m.get(Name.arg));
                        return Message.of(this, resultKey, op.apply(arg));
                    } catch (Exception e) {
                        return Message.of(this, Name.error, e);
                    }
                })
                .orElseGet(() -> Message.of(this, Name.error, "null"));

    }
}
