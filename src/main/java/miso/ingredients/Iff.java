package miso.ingredients;

import miso.misc.Name;

import java.util.*;

import static miso.ingredients.Message.message;

public class Iff<T> extends Function<T> {

    private static final List<String> params = Arrays.asList(Name.onTrue, Name.onFalse, Name.condition);

    class StateIff extends State {
        // messages must not be propagated to branches before decision has been calculated
        List<Message> pendingForBranchPropagation = new ArrayList<>();
        Object onTrue;
        Object onFalse;
        Boolean decision;

        StateIff(Source source) {
            super(source);
        }
    }

    private Map<Function<?>, Map<String, List<String>>> propagationsOnTrue = new HashMap<>();
    private Map<Function<?>, Map<String, List<String>>> propagationsOnFalse = new HashMap<>();
    private List<Function> initAndFinalizeOnTrue = new ArrayList<>();
    private List<Function> initAndFinalizeOnFalse = new ArrayList<>();

    public void propagateOnFalse(String keyReceived, String keyToPropagate, Function target) {
        propagate(keyReceived, keyToPropagate, target, propagationsOnFalse);
    }

    public void addPropagationOnTrue(String keyReceived, String keyToPropagate, Function target) {
        propagate(keyReceived, keyToPropagate, target, propagationsOnTrue);
    }

    public void addInitAndFinalizeOnTrue(Function f) {
        initAndFinalizeOnTrue.add(f);
    }

    public void initAndFinalizeOnFalse(Function f) {
        initAndFinalizeOnFalse.add(f);
    }

    private Map<Function<?>, Map<String, List<String>>> getBranchPropagations(Boolean branch) {
        return branch ? propagationsOnTrue : propagationsOnFalse;
    }

    private List<Function> getBranchInitsAndFinalizations(Boolean branch) {
        return branch ? initAndFinalizeOnTrue : initAndFinalizeOnFalse;
    }

    @Override
    StateIff newState(Source source) {
        return new StateIff(source);
    }

    @Override
    protected boolean isParameter(String key) {
        // a hack. keep super.process from propagating anything except
        // initialization and finalization of the decision part
        return true;
    }

    public static Iff<Integer> iff() {
        Iff<Integer> result = new Iff<>();
        start(result);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processInner(Message m, State s) {
        //TODO: there's a bad dependecy on the implemenation of If, they should be logically separated
        // see also the use of Sif.isParameter
        StateIff state = (StateIff) s;

        if (hdlIncomingParams(m, state)) {
            return;
        }

        hdlPropagations(m, state);
    }

    private void hdlPropagations(Message m, StateIff state) {
        if (!computed(state.decision)) {
            propagateToConditionOnSpec(m);
            state.pendingForBranchPropagation.add(m);
        } else {
            propagate(m, getBranchPropagations(state.decision));
        }
    }

    private void propagateToConditionOnSpec(Message m) {
        //TODO: send only messages to the condition that actually should go there, but should do no harm as done now,
        // except for useless messaging in some cases
        propagate(m);
    }

    private boolean hdlIncomingParams(Message m, StateIff state) {
        // onTrue/onFalse values may be set not only by the execution of a branch but also directly as parameters for if
        // propagated by an enclosing function.
        // so although propagation and execution of the proper branch only happens once the condition has bee calculated,
        // still both onTrue and onFalse may get set

        if (m.hasKey(Name.onTrue)) {
            state.onTrue = m.value;
        } else if (m.hasKey(Name.onFalse)) {
            state.onFalse = m.value;
        }
        if (m.hasKey(Name.condition)) {
            state.decision = (Boolean) m.value;
            initializeBranch(state);
        }
        if (isTrue(state.decision) && computed(state.onTrue)) {
            returnResult((T) state.onTrue, m.source.withHost(this));
            finalizeBranch(state);
        }
        if (isFalse(state.decision) && computed(state.onFalse)) {
            returnResult((T) state.onFalse, m.source);
            finalizeBranch(state);
        }

        return params.contains(m.key);
    }

    private void initializeBranch(StateIff state) {
        Message initialize = message(Name.initializeComputation, null, state.source.withHost(this));
        getBranchInitsAndFinalizations(state.decision).forEach(d -> d.recieve(initialize));

        state.pendingForBranchPropagation.forEach(p -> propagate(p, getBranchPropagations(state.decision)));
        state.pendingForBranchPropagation.clear();
    }

    private void finalizeBranch(StateIff state) {
        Message finalize = message(Name.finalizeComputation, null, state.source.withHost(this));
        getBranchInitsAndFinalizations(state.decision)
                .forEach(d -> d.recieve(finalize));
    }

}
