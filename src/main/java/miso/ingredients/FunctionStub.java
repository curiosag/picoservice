package miso.ingredients;

public class FunctionStub<T> extends Function<T> {

    final String keyFunctionParameter;

    private FunctionStub(String keyFunctionParameter) {
        this.keyFunctionParameter = keyFunctionParameter;
    }

    public static <T> FunctionStub<T> of(String keyFuncStubbed) {
        FunctionStub<T> result = new FunctionStub<>(keyFuncStubbed);
        result.label("stub:" + keyFuncStubbed);
        Actresses.start(result);
        return result;
    }

    @Override
    protected State newState(Origin origin) {
        return new FunctionStubState(this, origin);
    }

    @Override
    protected boolean isParameter(String key) {
        return true;  // propagate nothing
    }

    @Override
    protected void process(Message message) {
        trace(message);

        FunctionStubState state = (FunctionStubState) getState(message.origin);
        if (message.key.equals(Name.result)) {
            removeState(state.origin);
            returnResult((T) message.value, state.origin.sender(this));
            return;
        }

        state.forward(message.origin(message.origin.sender(this)));
    }

    @Override
    protected void processInner(Message m, State s) {
    }
}
