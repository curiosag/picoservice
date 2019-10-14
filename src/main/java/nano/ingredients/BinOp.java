package nano.ingredients;


import java.io.Serializable;
import java.util.function.BiFunction;

public class BinOp<T extends Serializable, U extends Serializable, V extends Serializable> extends Function<V> {
    private static final long serialVersionUID = 0L;

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
    protected boolean belongsToMe(String key) {
        return key.equals(Name.leftArg) || key.equals(Name.rightArg);
    }

    public BinOp(BiFunction<T, U, V> op, java.util.function.Function<Object, T> tConverter, java.util.function.Function<Object, U> uConverter) {
        this.op = op;
        this.tConverter = tConverter;
        this.uConverter = uConverter;
    }

    @Override
    @SuppressWarnings("unchecked") //TODO state class decoder
    protected void processInner(Message m, State s) {
        BinOpState state = (BinOpState) s;

        if (state.leftArg == null) {
            state.leftArg = getValue(m, Name.leftArg);
        }
        if (state.rightArg == null) {
            state.rightArg = getValue(m, Name.rightArg);
        }
        if (state.leftArg != null && state.rightArg != null) {
            T left = tConverter.apply(state.leftArg);
            U right = uConverter.apply(state.rightArg);
            removeState(state.origin);
            returnResult(op.apply(left, right), m.origin.sender(this));
        }
    }

}
