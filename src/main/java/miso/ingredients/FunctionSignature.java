package miso.ingredients;

import static miso.ingredients.Actresses.start;
import static miso.ingredients.Origin.origin;
import static miso.ingredients.trace.TraceMessage.traced;

public class FunctionSignature<T> extends Function<T> {

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

    public static <T> FunctionSignature<T> functionSignature(Function<T> body) {
        FunctionSignature<T> result = new FunctionSignature<>(body);
        start(result);
        return result;
    }

    protected boolean isPartialAppParam(Message m) {
        return false;
    }

    protected void pushPartialAppParamValue(Message m) {
    }

    protected void forwardPartialAppParamValues(FunctionSignatureState s) {
        s.setPartialApplicationValuesForwarded(true);
    }

    public void popPartialAppValues(Origin o) {
    }

    @Override
    public void process(Message m) {
        if (m.key.equals(Name.popPartialAppValues)) {
            popPartialAppValues(m.origin);
            return;
        }

        if (forwardingPartialAppParamValues(m)) {
            hackyTrace(m);
            super.process(m);
            return;
        }

        if (isPartialAppParam(m)) {
            pushPartialAppParamValue(m);
            return;
        }

        if (isDownstreamMessage(m)) {
            hackyFunctionCallTrace(m);
            Origin o = incCallLevel(m.origin);
            FunctionSignatureState state = (FunctionSignatureState) getState(o);
            state.caller = m.origin;
            state.triggerOfCaller = m.origin.scope;
            if (!state.partialApplicationValuesForwarded) {
                forwardPartialAppParamValues(state);
            }
            super.process(m.origin(o));
            return;
        }

        hackyTrace(m);
        super.process(m);
    }

    protected Origin incCallLevel(Origin o) {
        return origin(o.sender, o.sender, o.executionId, o.callLevel + 1, o.seqNr);
    }

    private boolean isDownstreamMessage(Message m) {
        return !(forwardingPartialAppParamValues(m)) && m.origin.sender instanceof FunctionCall;
    }

    private boolean forwardingPartialAppParamValues(Message m) {
        return m.origin.sender.equals(this) && isPartialAppParam(m);
    }

    private void hackyFunctionCallTrace(Message m) {
        // original scope is needed to reconstruct the trace
        Origin o = origin(m.origin.sender, m.origin.scope, m.origin.executionId, m.origin.callLevel + 1, m.origin.seqNr);
        hackyTrace(m.origin(o));
    }

    @Override
    protected void processInner(Message m, State s) {
        if (m.hasKey(Name.result)) {
            FunctionSignatureState state = (FunctionSignatureState) s;
            Function<?> triggerOfCaller = state.triggerOfCaller;
            Origin o = origin(this, triggerOfCaller, m.origin.executionId, m.origin.callLevel - 1, m.origin.seqNr + 1L);
            state.origin.scope.receive(m.origin(o));
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


    @Override
    protected void maybeTrace(Message message) {
        // bäh...
    }

    protected void hackyTrace(Message message) { // bäh..
        if (trace) {
            tracer.receive(traced(message, this));
        }
    }

}
