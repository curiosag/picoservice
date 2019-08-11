package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BinOp<T, V> extends Func<V> {

    private final BiFunction<T, T, V> op;
    private final Function<Object, T> converter;

    private Object leftArg;
    private Object rightArg;

    public BinOp(BiFunction<T, T, V> op, Function<Object, T> converter) {
        this.op = op;
        this.converter = converter;
    }

    @Override
    protected void process(Message m) {
        if (leftArg == null && m.hasKey(Name.leftArg)) {
            leftArg = m.value;
        }
        if (rightArg == null && m.hasKey(Name.rightArg)) {
            rightArg = m.value;
        }
        if (leftArg != null && rightArg != null) {
            try {
                T left = convert(leftArg);
                T right = convert(rightArg);
                try {
                    send(op.apply(left, right), m.opId);
                } catch (Exception e) {
                    throw new IllegalStateException();
                }
                leftArg = null;
                rightArg = null;
            } catch (Exception e) {
                throw new IllegalStateException();
            }
        }
    }

    private T convert(Object leftArg) {
        try {
            return converter.apply(leftArg);
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

}
