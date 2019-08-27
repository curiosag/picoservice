package miso.ingredients;

import miso.misc.Name;

import java.util.Arrays;
import java.util.List;

public class If<T> extends Function<T> {


    private static final List<String> parameters = Arrays. asList(Name.condition, Name.onFalse, Name.onTrue);

    class StateIf extends State {
        Object onTrue;
        Object onFalse;
        Boolean decision;

        StateIf(Source source) {
            super(source);
        }

        public boolean allComputed(){
            return computed(onFalse, onTrue, decision);
        }
    }

    @Override
    If.StateIf newState(Source source) {
        return new If.StateIf(source);
    }

    @Override
    protected boolean isParameter(String key) {
        return parameters.contains(key);
    }

    public static If<Integer> createIf() {
        If<Integer> result = new If<>();
        start(result);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processInner(Message m, State s) {
        If.StateIf state = (If.StateIf) s;

        if (m.hasKey(Name.condition)) {
            state.decision = (Boolean) m.value;
        }

        if (m.hasKey(Name.onTrue)) {
            state.onTrue = m.value;
        }

        if (m.hasKey(Name.onFalse)) {
            state.onFalse = m.value;
        }

        if (isTrue(state.decision) && computed(state.onTrue)) {
            returnResult((T) state.onTrue, m.source);
        }

        if (isFalse(state.decision) && computed(state.onFalse)) {
            returnResult((T) state.onFalse, m.source);
        }

        if (state.allComputed()){
            removeState(state.source);
        }
    }


}
