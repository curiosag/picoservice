package nano.ingredients;

import nano.ingredients.tuples.SerializableTuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static nano.ingredients.Ensemble.attachActor;
import static nano.ingredients.Origin.origin;

public class FunctionSignature<T extends Serializable> extends Function<T> {

    public final Function<T> body;
    public List<String> paramList = new ArrayList<>();
    public List<String> letKeys = new ArrayList<>();

    /*  FunctionSignature's responsibility is
     *
     *   - to serve multiple callers by returning the result to "origin.sender" of a state managed
     *     instead of the usual routine of using the "returnTo" target.
     *
     * */

    public FunctionSignature<T> paramList(String ... params) {
        paramList.addAll(Arrays.asList(params));
        return this;
    }

    public FunctionSignature<T> letKeys(String ... letKeys) {
        this.letKeys.addAll(Arrays.asList(letKeys));
        return this;
    }

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

        FuntionSignatureState state = (FuntionSignatureState) getState(m.origin);
        if (m.key.equals(Name.error)) {
            state.origin.getSender().tell(m.origin(m.origin.sender(this)));
            removeState(state.origin);
        }
        if (m.hasKey(returnKey) || letKeys.contains(m.key)) {
            super.process(m);
            return;
        }
        if (m.origin.sender == this && ! isConst(m.key)) {
            if (m.key.equals(Name.stackFrame)) {
                state.paramValues.forEach((k, v) -> super.process(new Message(k, v, m.origin)));
                return;
            } else {
                super.process(m);
            }
            return;
        }

        if(! m.hasKey(returnKey)) {
            state.addParamValue(m.key, m.getValue());
            if (state.paramValuesComplete()) {
                tell(new Message(Name.stackFrame, state.paramValues, m.origin.sender(this)));
            }
        }
    }

    @Override
    protected void processInner(Message m, FunctionState state) {
        if (m.hasKey(returnKey)) {
            hdlForwardings(state.origin, onReturn);
            removeState(state.origin);
            Origin o = origin(this, m.origin.getComputationPath(), m.origin.prevFunctionCallId, m.origin.lastFunctionCallId);
            state.origin.getSender().tell(m.origin(o));
        }
    }

    @Override
    protected FunctionState newState(Origin origin) {
        return new FuntionSignatureState(origin, paramList, letKeys);
    }

    @Override
    void propagate(String keyReceived, String keyToPropagate, Function targetFunc, Map<String, List<SerializableTuple<String, Function<?>>>> propagations) {
        if (! paramList.contains(keyReceived))
        {
            paramList.add(keyReceived);
        }
        super.propagate(keyReceived, keyToPropagate, targetFunc, propagations);
    }

    @Override
    protected boolean shouldPropagate(String key) {
        return !Name.result.equals(key);
    }

}
