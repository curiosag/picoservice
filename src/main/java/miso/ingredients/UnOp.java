package miso.ingredients;


public class UnOp<T, V> extends Function<V> {

    private final java.util.function.Function<T, V> op;
    private final java.util.function.Function<Object, T> converter;

    public class UnOpState extends State {
        Object arg;

        UnOpState(Origin origin) {
            super(origin);
        }
    }

    @Override
    protected State newState(Origin origin) {
        return new UnOpState(origin);
    }

    @Override
    protected boolean isParameter(String key) {
        return key.equals(Name.arg);
    }

    public UnOp(java.util.function.Function<T, V> op, java.util.function.Function<Object, T> converter) {
        this.op = op;
        this.converter = converter;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processInner(Message m, State s) {
        UnOpState state = (UnOpState) s;

        if (state.arg == null) {
            state.arg = getValue(m, Name.arg);
        }
        if (state.arg != null) {
            returnResult(op.apply(converter.apply(state.arg)), m.origin.sender(this));
            removeState(state.origin);
        }
    }


}
