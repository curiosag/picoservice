package miso.ingredients;

import miso.misc.Name;

import java.util.function.BiFunction;

public class BinOp<T, V> extends Function<V> {

    private final BiFunction<T, T, V> op;
    private final java.util.function.Function<Object, T> converter;

    public class BinOpState extends State {

        Object leftArg;
        Object rightArg;

        BinOpState(Source source) {
            super(source);
        }
    }

    @Override
    State newState(Source source) {
        return new BinOpState(source);
    }

    @Override
    protected boolean isParameter(String key) {
        return key.equals(Name.leftArg) || key.equals(Name.rightArg);
    }

    BinOp(BiFunction<T, T, V> op, java.util.function.Function<Object, T> converter) {
        this.op = op;
        this.converter = converter;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processInner(Message m, State s) {
        BinOpState state = (BinOpState) s;

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
                    returnResult(op.apply(left, right), m.source.withHost(this));
                } catch (Exception e) {
                    throw new IllegalStateException();
                }
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

    static java.util.function.Function<Object, Integer> intConverter = o -> (Integer) o;

    public static miso.ingredients.BinOp<Integer, Boolean> eq() {
        BinOp<Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 == n2, intConverter);
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Boolean> lt() {
        BinOp<Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 < n2, intConverter);
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Boolean> gt() {
        BinOp<Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 > n2, intConverter);
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Boolean> lteq() {
        BinOp<Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 <= n2, intConverter);
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Boolean> gteq() {
        BinOp<Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 >= n2, intConverter);
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer> add() {
        BinOp<Integer, Integer> result = new BinOp<>((n1, n2) -> n1 + n2, intConverter);
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer> sub() {
        BinOp<Integer, Integer> result = new BinOp<>((n1, n2) -> n1 - n2, intConverter);
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer> mul() {
        BinOp<Integer, Integer> result = new BinOp<>((n1, n2) -> n1 * n2, intConverter);
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer> div() {
        BinOp<Integer, Integer> result = new BinOp<>((n1, n2) -> n1 / n2, intConverter);
        start(result);
        return result;
    }
}
