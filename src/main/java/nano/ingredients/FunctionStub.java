package nano.ingredients;

import java.io.Serializable;

import static nano.ingredients.FunctionCall.functionCall;
import static nano.ingredients.Message.message;

public class FunctionStub<T extends Serializable> extends Function<T> {

    final String keyFunctionParameter;

    private FunctionStub(String keyFunctionParameter) {
        this.keyFunctionParameter = keyFunctionParameter;
    }

    public static <T extends Serializable> FunctionStub<T> of(String keyFuncStubbed) {
        FunctionStub<T> result = new FunctionStub<>(keyFuncStubbed);
        result.label("stub:" + keyFuncStubbed);
        Ensemble.attachActor(result);
        return result;
    }

    @Override
    protected FunctionState newState(Origin origin) {
        return new FunctionStubState(origin);
    }

    @Override
    protected boolean shouldPropagate(String key) {
        return false;  // propagate nothing
    }

    @Override
    public void process(Message m) {
        trace(m);
        //TODO maybe: FunctionSignature and PartialFunctionApplication can come in lie of stub
        // one FunctionSignature can be used multipe times in different stubs, PartialApp can not.
        // due to: actual.returnTo(state.functionCall, Name.result);
        FunctionStubState state = (FunctionStubState) getState(m.origin);
        if (m.key.equals(keyFunctionParameter)) {
            if (!(m.getValue() instanceof Function)) {
                throw new IllegalStateException();
            }
            tell(new Message(Name.createFunctionCall, ((Function) m.getValue()).address.id, m.origin.sender(this)));
        } else
            switch (m.key) {
                case Name.createFunctionCall: {
                    Long signatureId = (Long) m.getValue();
                    Function actual = (Function) Ensemble.resolve(signatureId);

                    state.functionCall = functionCall(actual);
                    state.functionCall.label(actual.address.label.toLowerCase());
                    state.functionCall.returnTo(this, Name.result);

                    if (actual instanceof PartialFunctionApplication)
                    {
                        actual.returnTo(state.functionCall, Name.result);
                    }

                    state.pendingForPropagation.forEach(state.functionCall::tell);
                    state.pendingForPropagation.clear();
                    break;
                }
                case Name.result: {
                    //TODO: somewhere the related akktor should be stopped some day
                    removeState(state.origin);
                    returnResult((T) m.getValue(), state.origin.sender(this));
                    break;
                }
                case Name.error: {
                    removeState(state.origin);
                    returnTo.aRef().tell(message(m.key, m.getValue(), m.origin.sender(this)), this.aRef());
                    break;
                }
                default: {
                    Message propaganda = m.origin(m.origin.sender(this));

                    if (state.functionCall != null) {
                        state.functionCall.tell(propaganda);
                    } else {
                        state.pendingForPropagation.add(propaganda);
                    }
                }
            }

    }


    @Override
    protected void processInner(Message m, FunctionState s) {
    }
}
