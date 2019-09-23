package miso.ingredients;

import miso.ingredients.guards.Guards;

import java.util.ArrayList;
import java.util.List;

import static miso.ingredients.Actresses.start;

public class ConditionalPropagation extends Function {

    private final List<String> priorityParams = new ArrayList<>();

    class StateConditionalPropagation extends State {
        List<Message> otherParamsPending = new ArrayList<>();
        List<Message> priorityParamsEncountered = new ArrayList<>();

        StateConditionalPropagation(Origin origin) {
            super(origin);
        }

        public boolean allMandatoryParamsEncountered() {
            return priorityParamsEncountered.size() == priorityParams.size();
        }
    }

    public ConditionalPropagation addPriorityParam(String key) {
        priorityParams.add(key);
        return this;
    }

    @Override
    protected StateConditionalPropagation newState(Origin origin) {
        return new StateConditionalPropagation(origin);
    }

    @Override
    protected boolean isParameter(String key) {
        return true;
    }

    public static ConditionalPropagation conditionalPropagation() {
        ConditionalPropagation result = new ConditionalPropagation();
        start(result);
        return result;
    }

    @Override
    protected void processInner(Message m, State state) {
    }

    @Override
    public void process(Message m) {
        StateConditionalPropagation state = (StateConditionalPropagation) getState(m.origin);

        if(m.key.equals(Name.result)){
            throw new IllegalStateException();
        }

        if(m.key.equals(Name.removeState)){
            removeState(state.origin);
            return;
        }

        if (state.allMandatoryParamsEncountered()) {
            Guards.isFalse(priorityParams.contains(m.key));
            super.propagate(m);
            return;
        }

        if (priorityParams.contains(m.key)) {
            state.priorityParamsEncountered.add(m);
            super.propagate(m, Acknowledge.Y);
        } else {
            state.otherParamsPending.add(m);
        }

        if (state.allMandatoryParamsEncountered()) {
            state.otherParamsPending.forEach(super::propagate);
        }

    }

}
