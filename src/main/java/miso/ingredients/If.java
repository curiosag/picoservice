package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class If<T> extends Func<T> {

    class StateIf extends State {
        Object onTrue;
        Object onFalse;
        Boolean decision;
        final Set<String> keysExpected;

        StateIf(Source source, Set<String> keysExpected) {
            super(source);
            this.keysExpected = keysExpected;
        }
    }

    private final List<String> keysExpected = Arrays.asList(Name.decision, Name.onFalse, Name.onTrue);

    @Override
    If.StateIf newState(Source source) {
        Set<String> keys = propagationKeysExpected();
        keys.addAll(keysExpected());
        return new If.StateIf(source, keys);
    }

    @Override
    List<String> keysExpected() {
        return keysExpected;
    }

    private If() {
    }

    public static If<Integer> condInt(Func<Boolean> cond) {
        return new If<>();
    }

    @Override
    protected void process(Message m) {
        If.StateIf state = (If.StateIf) getState(m.source);

        if (keysExpected.contains(m.key))
        {
            propagate(m);
            return;
        }

        state.keysExpected.remove(m.key);

        if (state.decision == null && m.hasKey(Name.decision)) {
            state.decision = (Boolean) m.value;
        }

        if (m.hasKey(Name.onTrue)) {
            state.onTrue = m.value;
        }

        if (m.hasKey(Name.onFalse)) {
            state.onFalse = m.value;
        }

        if (computed(state.decision)) {
            if (state.decision && computed(state.onTrue)) {
                returnResult((T) state.onTrue, m.source);

            }
            if (!state.decision && computed(state.onFalse)) {
                {
                    returnResult((T) state.onFalse, m.source);
                }
            }
        }

        if (keysExpected.size() == 0) {
            removeState(m.source);
        }
    }

}
