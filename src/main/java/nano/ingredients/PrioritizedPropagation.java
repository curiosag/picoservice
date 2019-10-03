package nano.ingredients;

import nano.ingredients.guards.Guards;

import java.util.ArrayList;
import java.util.List;

import static nano.ingredients.Ensemble.wire;

public class PrioritizedPropagation extends Function {

    private final List<String> priorityParams = new ArrayList<>();

    class PrioritizedPropagationState extends State {
        List<Message> otherParamsPending = new ArrayList<>();
        List<Message> priorityParamsProcessedDownstream = new ArrayList<>();

        PrioritizedPropagationState(Origin origin) {
            super(origin);
        }

        public boolean allPriorityParamsProcessedDownstream() {
            return priorityParamsProcessedDownstream.size() == priorityParams.size();
        }
    }

    public PrioritizedPropagation addPriorityParam(String key) {
        priorityParams.add(key);
        return this;
    }

    @Override
    protected PrioritizedPropagationState newState(Origin origin) {
        return new PrioritizedPropagationState(origin);
    }

    @Override
    protected boolean isParameter(String key) {
        return true;
    }

    public static PrioritizedPropagation prioritizedPropagation() {
        PrioritizedPropagation result = new PrioritizedPropagation();
        wire(result);
        return result;
    }

    @Override
    protected void processInner(Message m, State state) {
    }

    @Override
    protected void onAck(Message m) {
        Guards.isTrue(priorityParams.contains(m.key));

        PrioritizedPropagationState state = (PrioritizedPropagationState) getState(m.origin);
        Guards.isFalse(state.priorityParamsProcessedDownstream.stream().anyMatch(i -> i.key.equals(m.key)));

        state.priorityParamsProcessedDownstream.add(m);
        if (state.allPriorityParamsProcessedDownstream()) {
            state.otherParamsPending.forEach(super::propagate);
        }
    }

    @Override
    public void process(Message m) {
        trace(m);

        if (m.key.equals(Name.removeState)) {
            removeState(m.origin);
            return;
        }

        if (m.key.equals(Name.result)) {
            throw new IllegalStateException();
        }

        PrioritizedPropagationState state = (PrioritizedPropagationState) getState(m.origin);
        if (state.allPriorityParamsProcessedDownstream()) {
            Guards.isFalse(priorityParams.contains(m.key));
            super.propagate(m);
        } else {
            if (priorityParams.contains(m.key)) {
                super.propagate(m, Acknowledge.Y);
            } else {
                state.otherParamsPending.add(m);
            }

        }
    }

}
