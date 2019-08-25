package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.*;

public class SIf<T> extends Function<T> {

    private static final List<String> params = Arrays.asList(Name.onTrue, Name.onFalse, Name.condition);

    class StateSIf extends State {
        // messages must not be propagated to branches before decision has been calculated
        List<Message> pendingForBranchPropagation = new ArrayList<>();
        Object onTrue;
        Object onFalse;
        Boolean decision;

        StateSIf(Source source) {
            super(source);
        }
    }

    private Map<Function<?>, Map<String, List<String>>> propagationsOnTrue = new HashMap<>();
    private Map<Function<?>, Map<String, List<String>>> propagationsOnFalse = new HashMap<>();
    private List<Function> initAndFinalizeOnTrue = new ArrayList<>();
    private List<Function> initAndFinalizeOnFalse = new ArrayList<>();

    public void addPropagationOnFalse(String keyReceived, String keyToPropagate, Function target) {
        addPropagation(keyReceived, keyToPropagate, target, propagationsOnFalse);
    }

    public void addPropagationOnTrue(String keyReceived, String keyToPropagate, Function target) {
        addPropagation(keyReceived, keyToPropagate, target, propagationsOnTrue);
    }

    public void addInitAndFinalizeOnTrue(Function f) {
        initAndFinalizeOnTrue.add(f);
    }

    public void addInitAndFinalizeOnFalse(Function f) {
        initAndFinalizeOnFalse.add(f);
    }

    private Map<Function<?>, Map<String, List<String>>> getBranchPropagations(Boolean branch) {
        return branch ? propagationsOnTrue : propagationsOnFalse;
    }

    private List<Function> getBranchInitsAndFinalizations(Boolean branch) {
        return branch ? initAndFinalizeOnTrue : initAndFinalizeOnFalse;
    }

    @Override
    SIf.StateSIf newState(Source source) {
        return new SIf.StateSIf(source);
    }

    @Override
    protected boolean isParameter(String key) {
        // a hack. keep super.process from propagating anything except
        // initialization and finalization of the decision part
        return true;
    }

    public static SIf<Integer> condInt() {
        return new SIf<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processInner(Message m, State s) {
        //TODO: there's a bad dependecy on the implemenation of If, they should be logically separated
        // see also the use of Sif.isParameter
        SIf.StateSIf state = (SIf.StateSIf) s;

        if (hdlIncomingParams(m, state)) {
            return;
        }

        hdlPropagations(m, state);
    }

    private void hdlPropagations(Message m, StateSIf state) {
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

    private boolean hdlIncomingParams(Message m, StateSIf state) {
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

    private void initializeBranch(StateSIf state) {
        Message initialize = new Message(Name.initializeComputation, null, state.source.withHost(this));
        getBranchInitsAndFinalizations(state.decision).forEach(d -> d.recieve(initialize));

        state.pendingForBranchPropagation.forEach(p -> propagate(p, getBranchPropagations(state.decision)));
        state.pendingForBranchPropagation.clear();
    }

    private void finalizeBranch(StateSIf state) {
        Message finalize = new Message(Name.finalizeComputation, null, state.source.withHost(this));
        getBranchInitsAndFinalizations(state.decision)
                .forEach(d -> d.recieve(finalize));
    }

}
