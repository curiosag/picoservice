package nano.ingredients;

import static nano.ingredients.Message.message;

public class FunctionStub<T> extends Function<T> {

    final String keyFunctionParameter;

    private FunctionStub(String keyFunctionParameter) {
        this.keyFunctionParameter = keyFunctionParameter;
    }

    public static <T> FunctionStub<T> of(String keyFuncStubbed) {
        FunctionStub<T> result = new FunctionStub<>(keyFuncStubbed);
        result.label("stub:" + keyFuncStubbed);
        Actresses.wire(result);
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
    public void process(Message m) {
        trace(m);

        FunctionStubState state = (FunctionStubState) getState(m.origin);
        switch (m.key) {
            case Name.result: {
                removeState(state.origin);
                returnResult((T) m.value, state.origin.sender(this));
                break;
            }
            case Name.error: {
                removeState(state.origin);
                returnTo.aRef().tell(message(m.key, m.value, m.origin.sender(this)), this.aRef());
                break;
            }
            default:
                state.forward(m.origin(m.origin.sender(this)));
        }

    }

    @Override
    protected void processInner(Message m, State s) {
    }
}
