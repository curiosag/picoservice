package nano.ingredients;

import java.io.Serializable;

import static nano.ingredients.Ensemble.wire;
import static nano.ingredients.Origin.origin;

public class FunctionSignature<T extends Serializable> extends Function<T> {

    public final Function<T> body;

    /*  FunctionSignature's responsibility are.
     *   - increment the call level for incoming messages and decrement it again for the result
     *     this only works if used together with a functionCall. It also means that messages coming from the body
     *     must not be sent from functionCalls directly, othewise the callLevel counting won't work
     *
     *   - to serve multiple callers by returning the result to "origin.sender" of a state managed
     *     instead of the usual routine of using the "returnTo" target.
     *
     *   - handling for partially applied functions. works only if separate instances of FunctionSignature
     *     are used in each lambda. There's no way to tell apart stuff otherwise
     *
     * */


    protected FunctionSignature(Function<T> body) {
        this.body = body;
        body.returnTo(this, Name.result);
    }

    public static <T extends Serializable > FunctionSignature<T> functionSignature(Function<T> body) {
        FunctionSignature<T> result = new FunctionSignature<>(body);
        wire(result);
        return result;
    }

    protected boolean isPartialAppParam(Message m) {
        return false;
    }

    protected void setPartialAppParamValue(Message m) {
    }

    protected void forwardPartialAppParamValues(FunctionSignatureState s) {
        s.setPartialApplicationValuesForwarded(true);
    }

    public void removePartialAppValues(Origin o) {
    }

    @Override
    public void process(Message m) {
        trace(m);

        if (m.key.equals(Name.removePartialAppValues)) {
            removePartialAppValues(m.origin);
            return;
        }


        if (forwardingPartialAppParamValues(m)) {
            super.process(m);
            return;
        }

        if (isPartialAppParam(m)) {
            setPartialAppParamValue(m);
            return;
        }

        if(m.key.equals(Name.error))
        {
            FunctionSignatureState state = (FunctionSignatureState) getState(m.origin);
            state.origin.getSender().tell(m.origin(m.origin.sender(this)));
            removeState(state.origin);
        }

        if (isDownstreamMessage(m)) {
            FunctionSignatureState state = (FunctionSignatureState) getState(m.origin);
            if (!state.partialApplicationValuesForwarded) {
                forwardPartialAppParamValues(state);
            }
            super.process(m);
            return;
        }

        super.process(m);
    }

    private boolean isDownstreamMessage(Message m) {
        return (m.origin.getSender() instanceof FunctionCall);
    }

    private boolean forwardingPartialAppParamValues(Message m) {
        return m.origin.getSender().equals(this) && isPartialAppParam(m);
    }

    @Override
    protected void processInner(Message m, State s) {
        if (m.hasKey(Name.result)) {
            FunctionSignatureState state = (FunctionSignatureState) s;
            hdlForwarings(state.origin, onReturn);
            Origin o = origin(this, m.origin.getComputationBough(), m.origin.prevFunctionCallId, m.origin.lastFunctionCallId);
            state.origin.getSender().tell(m.origin(o));
            removeState(state.origin);
        }
    }

    @Override
    protected State newState(Origin origin) {
        return new FunctionSignatureState(origin);
    }

    @Override
    protected boolean isParameter(String key) {
        return Name.result.equals(key);
    }


}
