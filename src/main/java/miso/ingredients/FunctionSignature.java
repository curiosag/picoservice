package miso.ingredients;

import static miso.ingredients.Actresses.start;
import static miso.ingredients.Origin.origin;

public class FunctionSignature<T> extends Function<T> {

    public final Function<T> body;
    private boolean peep;


    public void peep() {
        this.peep = true;
    }


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

    public static <T> FunctionSignature<T> functionSignature(Function<T> body) {
        FunctionSignature<T> result = new FunctionSignature<>(body);
        start(result);
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
        if(peep)
        {
            debug(m, m.origin.sender(this), " signature received ");
        }

         if (m.key.equals(Name.removePartialAppValues)) {
            removePartialAppValues(m.origin);
            return;
        }

        trace(m);

        if (forwardingPartialAppParamValues(m)) {
            super.process(m);
            return;
        }

        if (isPartialAppParam(m)) {
            setPartialAppParamValue(m);
            return;
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
        return (m.origin.sender instanceof FunctionCall) && !(m.key.equals(Name.result));
    }

    private boolean forwardingPartialAppParamValues(Message m) {
        return m.origin.sender.equals(this) && isPartialAppParam(m);
    }

    @Override
    protected void processInner(Message m, State s) {
        if (m.hasKey(Name.result)) {
            FunctionSignatureState state = (FunctionSignatureState) s;
            hdlOnReturns(state.origin, onReturn);
            Origin o = origin(this, m.origin.executionId, m.origin.seqNr + 1L, m.origin.callStack);
            if(peep)
            {
                debug(m.origin(o), o, " signature returns ");
            }
            state.origin.sender.receive(m.origin(o));
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
