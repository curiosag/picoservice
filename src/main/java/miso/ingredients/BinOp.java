package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BinOp<T, V> extends Func<V> {

    private final BiFunction<T, T, V> op;
    private final Function<Object, T> converter;

    public class BinOpState extends State {

        Object leftArg;
        Object rightArg;

        public BinOpState(Source source) {
            super(source);
        }
    }

    @Override
    State newState(Source source) {
        return new BinOpState(source);
    }

    @Override
    List<String> keysExpected() {
        return null;
    }

    public BinOp(BiFunction<T, T, V> op, Function<Object, T> converter) {
        this.op = op;
        this.converter = converter;
    }

    @Override
    protected void process(Message m) {
        BinOpState state = (BinOpState) getState(m.source);

        if (state.leftArg == null) {
            state.leftArg = getValue(m, Name.leftArg);
        }
        if (state.rightArg == null) {
            state.rightArg = getValue(m, Name.rightArg);
        }
        if (state.leftArg != null && state.rightArg != null) {
            try {
                T left = convert(state.leftArg);
                T right = convert(state.rightArg);
                try {
                    returnResult(op.apply(left, right), m.source);
                } catch (Exception e) {
                    throw new IllegalStateException();
                }
                removeState(state.source);
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
