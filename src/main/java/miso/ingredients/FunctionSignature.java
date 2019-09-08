package miso.ingredients;

import static miso.ingredients.Actresses.start;
import static miso.ingredients.Origin.origin;
import static miso.ingredients.trace.TraceMessage.traced;

public class FunctionSignature<T> extends Function<T> {

    final Function<T> body;


    /*  FunctionSignature's responsibility are.
     *   - increment the call level for incoming messages and decrement it again for the result
     *     this only works if used together with a functionCall. It also means that messages coming from the body
     *     must not be sent from functionCalls directly, othewise the callLevel counting won't work
     *
     *   - to serve multiple callers by returning the result to "origin.sender" of a state managed
     *     instead of the usual routine of using the "returnTo" target.
     *
     *   - handling for partially applied functions. works only if separate instances of FunctionSignature
     *     are used in each lambda. If that's not done, then:
     *      if e.g 2 lambdas l1, l2 both have the same FunctionSignature S.
     *      and The partial application values get set for S
     *      and l1 and l2 get passed to 2 different function calls c1, c2.
     *      and downstream c1 pushes another set of partial application values
     *      TODO: then there's no way ???????????????????????????
     *
     * */


    FunctionSignature(Function<T> body) {
        this.body = body;
        body.returnTo(this, Name.result);
    }

    public static <T> FunctionSignature<T> functionSignature(Function<T> body) {
        FunctionSignature<T> result = new FunctionSignature<>(body);
        start(result);
        return result;
    }

    protected boolean isPartialAppParamValue(Message m) {
        return false;
    }

    protected void pushPartialAppParamValue(Message m) {
    }

    protected void forwardPartialAppParamValues(FunctionSignatureState o) {
        o.setPartialApplicationValuesForwarded(true);
    }

    @Override
    public void process(Message m) {
        if (isDownstreamMessage(m)) {
            hackyFunctionCallTrace(m);
            Origin o = origin(m.origin.sender, m.origin.sender, m.origin.executionId, m.origin.callLevel + 1, m.origin.seqNr);
            FunctionSignatureState state = (FunctionSignatureState) getState(o);
            state.caller = m.origin;
            //TODO clean this up
            state.triggerOfCaller = m.origin.scope;
            if (isPartialAppParamValue(m)) {
                pushPartialAppParamValue(m);
            } else {
                forwardPartialAppParamValues(state);
                super.process(m.origin(o));
            }

        }
        if (isUpstreamMessage(m) || isSelfMessage(m)) {
            hackyTrace(m);
            super.process(m);
        }
    }

    private boolean isUpstreamMessage(Message m) {
        return !isDownstreamMessage(m) && !isSelfMessage(m);
    }

    private boolean isDownstreamMessage(Message m) {
        return !(isSelfMessage(m)) && m.origin.sender instanceof FunctionCall;
    }

    private boolean isSelfMessage(Message m) {
        return m.origin.partiallyApplied;
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
