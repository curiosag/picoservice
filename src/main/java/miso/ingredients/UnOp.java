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
    protected void process(Message m) {
        try {
            T arg = converter.apply(m.value);
            send(op.apply(arg), m.opId);
        } catch (Exception e) {
            send(null, m.opId);
        }
    }
}
