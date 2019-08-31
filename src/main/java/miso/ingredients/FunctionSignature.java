package miso.ingredients;

import static miso.ingredients.Origin.origin;

public class FunctionSignature<T> extends Function<T> {

    /*  FunctionSignature's responsibility are.
     *   - increment the call level for incoming messages and decrement it again for the result
     *     this only works if used together with a functionCall. It also means that messages coming from the body
     *     must not be sent from functionCalls directly, othewise the callLevel counting won't work
     *
     *   - to serve multiple callers by returning the result to the origin.sender of a state managed
     *
     *
     *   I would be possible to keep one result key per state managed, but one would
     *   need to set it passing in a message, which seems rather messy
     * */
    private FunctionSignature(Function<T> body) {
        body.returnTo(this, Name.result);
    }

    public static <T> FunctionSignature<T> functionSignature(Function<T> body) {
        FunctionSignature<T> result = new FunctionSignature<>(body);
        start(result);
        return result;
    }

    @Override
    protected State newState(Origin origin) {
        return new FunctionSignatureState(origin);
    }

    @Override
    protected boolean isParameter(String key) {
        return Name.result.equals(key);
    }

    @Override
    public void process(Message m) {
        if (m.origin.sender instanceof FunctionCall) {
            Origin o = origin(m.origin.sender, m.origin.sender, m.origin.executionId, m.origin.callLevel + 1);
            FunctionSignatureState state = (FunctionSignatureState) getState(o);
            state.triggerOfCaller = m.origin.scope;
            super.process(m.origin(o));

        } else
            super.process(m);
    }

    @Override
    protected void processInner(Message m, State state) {
        if (m.hasKey(Name.result)) {
            Function<?> triggerOfCaller = ((FunctionSignatureState) state).triggerOfCaller;
            Origin o = origin(this, triggerOfCaller, m.origin.executionId, m.origin.callLevel - 1);
            state.origin.scope.receive(m.origin(o));
            removeState(state.origin);
        }
    }

}
