package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.function.Function;

public class UnOp<T, V> extends Func {

    private final Function<T, V> op;
    private final Function<Object, T> converter;

    public UnOp(Function<T, V> op, Function<Object, T> converter) {
        this.op = op;
        this.converter = converter;
        paramsRequired(Name.arg);
    }

    @Override
    protected Message getNext() {
        return getCurrent()
                .map(m -> {
                    try {
                        T arg = converter.apply(m.get(Name.arg));
                        return Message.of(resultKey, op.apply(arg));
                    } catch (Exception e) {
                        return Message.of(Name.error, e);
                    }
                })
                .orElseGet(() -> Message.of(Name.error, "null"));

    }
}
