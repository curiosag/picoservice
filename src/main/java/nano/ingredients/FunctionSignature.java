package nano.ingredients;

import java.io.Serializable;

import static nano.ingredients.Ensemble.attachActor;
import static nano.ingredients.Origin.origin;

public class FunctionSignature<T extends Serializable> extends Function<T> {

    public final Function<T> body;

    /*  FunctionSignature's responsibility is
     *
     *   - to serve multiple callers by returning the result to "origin.sender" of a state managed
     *     instead of the usual routine of using the "returnTo" target.
     *
     * */

    protected FunctionSignature(Function<T> body) {
        this.body = body;
        body.returnTo(this, Name.result);
    }

    public static <T extends Serializable> FunctionSignature<T> functionSignature(Function<T> body) {
        FunctionSignature<T> result = new FunctionSignature<>(body);
        attachActor(result);
        return result;
    }

    @Override
    public void process(Message m) {
        trace(m);

        if (m.key.equals(Name.error)) {
            State state = getState(m.origin);
            state.origin.getSender().tell(m.origin(m.origin.sender(this)));
            removeState(state.origin);
        }

        super.process(m);
    }

    @Override
    protected void processInner(Message m, State state) {
        if (m.hasKey(Name.result)) {
            hdlForwarings(state.origin, onReturn);
            Origin o = origin(this, m.origin.getComputationPath(), m.origin.prevFunctionCallId, m.origin.lastFunctionCallId);
            state.origin.getSender().tell(m.origin(o));
            removeState(state.origin);
        }
    }

    @Override
    protected State newState(Origin origin) {
        return new State(origin);
    }

    @Override
    protected boolean belongsToMe(String key) {
        return Name.result.equals(key);
    }

}
