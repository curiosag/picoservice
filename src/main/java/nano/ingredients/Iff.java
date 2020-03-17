package nano.ingredients;

import nano.ingredients.tuples.SerializableKeyValuePair;
import nano.ingredients.tuples.ForwardingItem;
import nano.ingredients.tuples.SerializableTuple;

import java.io.Serializable;
import java.util.*;

import static nano.ingredients.Ensemble.attachActor;
import static nano.ingredients.Message.message;

public class Iff<T extends Serializable> extends Function<T> {

    private static final List<String> params = Arrays.asList(Name.onTrue, Name.onFalse, Name.condition);
    private final List<ForwardingItem> onReturnOnTrueSend = new ArrayList<>();
    private final List<ForwardingItem> onReturnOnFalseSend = new ArrayList<>();


    class StateIff extends FunctionState {
        // messages must not be propagated to branches before decision has been calculated
        List<Message> pendingForBranchPropagation = new ArrayList<>();
        Object onTrue;
        Object onFalse;
        Boolean decision;

        StateIff(Origin origin) {
            super(origin);
        }
    }

    private Map<String, List<SerializableTuple<String, Function<?>>>> propagationsOnTrue = new HashMap<>();
    private Map<String, List<SerializableTuple<String, Function<?>>>> propagationsOnFalse = new HashMap<>();
    private List<Function> kickOffOnTrue = new ArrayList<>();
    private List<Function> kickOffOnFalse = new ArrayList<>();


    public void onReturnOnTrueSend(String key, Serializable value, Function<?> target){
        onReturnOnTrueSend.add(ForwardingItem.of(SerializableKeyValuePair.of(key, value), target));
    }

    public void onReturnOnFalseSend(String key, Serializable value, Function<?> target){
        onReturnOnFalseSend.add(ForwardingItem.of(SerializableKeyValuePair.of(key, value), target));
    }

    public void propagateOnFalse(String keyReceived, String keyToPropagate, Function target) {
        propagate(keyReceived, keyToPropagate, target, propagationsOnFalse);
    }

    public void propagateOnTrue(String keyReceived, String keyToPropagate, Function target) {
        propagate(keyReceived, keyToPropagate, target, propagationsOnTrue);
    }

    public void kickOffOnTrue(Function f) {
        kickOffOnTrue.add(f);
    }

    public void kickOffOnFalse(Function f) {
        kickOffOnFalse.add(f);
    }

    private Map<String, List<SerializableTuple<String, Function<?>>>> getBranchPropagations(Boolean branch) {
        return branch ? propagationsOnTrue : propagationsOnFalse;
    }

    private List<Function> getBranchKickOffs(Boolean branch) {
        return branch ? kickOffOnTrue : kickOffOnFalse;
    }

    @Override
    protected StateIff newState(Origin origin) {
        return new StateIff(origin);
    }

    @Override
    protected boolean shouldPropagate(String key) {
        // a hack. make super.process only kickOf the decision part
        // nothing else, no propagation or anything
        return false;
    }

    public static Iff<Integer> iff() {
        Iff<Integer> result = new Iff<>();
        attachActor(result);
        return result;
    }

    public static Iff<ArrayList<Integer>> iffList() {
        Iff<ArrayList<Integer>> result = new Iff<>();
        attachActor(result);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processInner(Message m, FunctionState s) {
        //TODO: there's a bad dependecy on the implemenation of If, they should be logically separated
        // see also the use of Sif.shouldPropagate
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
            state.onTrue = m.getValue();
        } else if (m.hasKey(Name.onFalse)) {
            state.onFalse = m.getValue();
        }
        if (m.hasKey(Name.condition)) {
            state.decision = (Boolean) m.getValue();
            kickOffBranch(state);
        }
        if (isTrue(state.decision) && computed(state.onTrue)) {
            onReturnOnTrueSend.forEach(v -> v.target().tell(message(v.keyValuePair().key(), v.keyValuePair().value(), m.origin)));
            returnResult((T) state.onTrue, m.origin.sender(this));
            removeState(state.origin);
        }
        if (isFalse(state.decision) && computed(state.onFalse)) {
            onReturnOnFalseSend.forEach(v -> v.target().tell(message(v.keyValuePair().key(), v.keyValuePair().value(), m.origin)));
            returnResult((T) state.onFalse, m.origin.sender(this));
            removeState(state.origin);
        }

        return params.contains(m.key);
    }

    private void kickOffBranch(StateIff state) {
        Message kickOff = message(Name.kickOff, null, state.origin.sender(this));
        getBranchKickOffs(state.decision).forEach(d -> d.tell(kickOff));

        state.pendingForBranchPropagation.forEach(p -> propagate(p, getBranchPropagations(state.decision)));
        state.pendingForBranchPropagation.clear();
    }


}