package nano.ingredients;

import java.util.Arrays;
import java.util.List;

import static nano.ingredients.Actresses.wire;

public class If<T> extends Function<T> {


    private static final List<String> parameters = Arrays. asList(Name.condition, Name.onFalse, Name.onTrue);

    class StateIf extends State {
        Object onTrue;
        Object onFalse;
        Boolean decision;

        StateIf(Origin origin) {
            super(origin);
        }

        public boolean allComputed(){
            return computed(onFalse, onTrue, decision);
        }
    }

    @Override
    protected If.StateIf newState(Origin origin) {
        return new If.StateIf(origin);
    }

    @Override
    protected boolean isParameter(String key) {
        return parameters.contains(key);
    }

    public static If<Integer> createIf() {
        If<Integer> result = new If<>();
        wire(result);
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
            returnResult((T) state.onTrue, m.origin);
        }

        if (isFalse(state.decision) && computed(state.onFalse)) {
            returnResult((T) state.onFalse, m.origin);
        }

        if (state.allComputed()){
            removeState(state.origin);
        }
    }


}
