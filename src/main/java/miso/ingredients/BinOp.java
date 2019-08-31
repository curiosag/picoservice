package miso.ingredients;


import java.util.function.BiFunction;

public class BinOp<T, U, V> extends Function<V> {

    private final BiFunction<T, U, V> op;
    private final java.util.function.Function<Object, T> tConverter;
    private final java.util.function.Function<Object, U> uConverter;

    public class BinOpState extends State {

        Object leftArg;
        Object rightArg;

        BinOpState(Origin origin) {
            super(origin);
        }
    }

    @Override
    protected State newState(Origin origin) {
        return new BinOpState(origin);
    }

    @Override
    protected boolean isParameter(String key) {
        return key.equals(Name.leftArg) || key.equals(Name.rightArg);
    }

    BinOp(BiFunction<T, U, V> op, java.util.function.Function<Object, T> tConverter, java.util.function.Function<Object, U> uConverter) {
        this.op = op;
        this.tConverter = tConverter;
        this.uConverter = uConverter;
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
                T left = t(state.leftArg);
                U right = u(state.rightArg);
                removeState(state.origin);
                returnResult(op.apply(left, right), m.origin.sender(this));
            } catch (Exception e) {
                throw e;
            }
        }
    }

    private T t(Object leftArg) {
        return tConverter.apply(leftArg);
    }

    private U u(Object rightArg) {
        return uConverter.apply(rightArg);
    }

    public static miso.ingredients.BinOp<Boolean, Boolean, Boolean> and() {
        BinOp<Boolean, Boolean, Boolean> result = new BinOp<>((n1, n2) -> n1 && n2, boolConverter, boolConverter);
        result.label("&");
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer, Boolean> eq() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 == n2, intConverter, intConverter);
        result.label("==");
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer, Boolean> lt() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 < n2, intConverter, intConverter);
        result.label("<");
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer, Boolean> gt() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 > n2, intConverter, intConverter);
        result.label(">");
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer, Boolean> lteq() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 <= n2, intConverter, intConverter);
        result.label("<=");
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer, Boolean> gteq() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 >= n2, intConverter, intConverter);
        result.label(">=");
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer, Integer> add() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((n1, n2) -> n1 + n2, intConverter, intConverter);
        result.label("+");
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer, Integer> sub() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((n1, n2) -> n1 - n2, intConverter, intConverter);
        result.label("-");
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer, Integer> mul() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((n1, n2) -> n1 * n2, intConverter, intConverter);
        result.label("*");
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer, Integer> div() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((n1, n2) -> n1 / n2, intConverter, intConverter);
        result.label("div");
        start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, Integer, Integer> mod() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((n1, n2) -> n1 % n2, intConverter, intConverter);
        result.label("mod");
        start(result);
        return result;
    }

}
