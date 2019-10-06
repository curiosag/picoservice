package nano.ingredients;

import nano.ingredients.tuples.ComputationBoughBranch;
import nano.ingredients.tuples.ComputationOriginBranch;

import java.io.Serializable;
import java.util.Objects;

public class Origin implements Serializable {
    private static final long serialVersionUID = 0L;

    transient Function<?> sender; // not serialized, rehydrated by asking for Function for senderId
    public final long senderId;
    private final ComputationBough computationBough;
    final long prevFunctionCallId;
    final long lastFunctionCallId;

    Origin(Function<?> sender, ComputationBough computationBough, long lastFunctionCallId, long prevFunctionCallId) {
        this.senderId = sender.address.id;
        this.sender = sender;
        this.computationBough = computationBough;
        this.prevFunctionCallId = prevFunctionCallId;
        this.lastFunctionCallId = lastFunctionCallId;
    }

    public static Origin origin(Function<?> sender) {
        Long id = sender.address.id;
        return new Origin(sender, new ComputationBough(0L), id, -1L);
    }

    public static Origin origin(Function<?> sender, ComputationBough computationBough, long prevFunctionCallId, long lastFunctionCallId) {
        return new Origin(sender, computationBough, prevFunctionCallId, lastFunctionCallId);
    }

    Origin sender(Function<?> sender) {
        return origin(sender, computationBough, prevFunctionCallId, lastFunctionCallId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Origin)) {
            throw new IllegalStateException();
        }
        Origin origin = (Origin) o;
        return senderId == origin.senderId && // sender isn't necessarily in the call stack, can be something which is not a FunctionCall
                getExecutionId() == origin.getExecutionId() &&
                functionCallTreeLocation().getCallStack().equals(origin.functionCallTreeLocation().getCallStack());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSender(), getExecutionId(), computationBough);
    }

    private ComputationTreeLocation computationTreeLocation;

    public ComputationTreeLocation functionCallTreeLocation() {
        if (computationTreeLocation == null) {
            computationTreeLocation = new ComputationTreeLocation(this);
        }
        return computationTreeLocation;
    }

    Origin popCall() {
        ComputationBough bough = computationBough.pop();
        return new Origin(getSender(), bough, bough.getLastPopped(), lastFunctionCallId);
    }

    ComputationOriginBranch pushCall(FunctionCall functionCall) {
        ComputationBoughBranch maybeBranch = computationBough.push(functionCall.address.id);

        Origin o = new Origin(getSender(), maybeBranch.getExecutionBough(), functionCall.address.id, lastFunctionCallId);
        return ComputationOriginBranch.of(o, maybeBranch.getBoughBranchedOffFrom());
    }

    public Function<?> getSender() {
        if (sender == null) {
            sender = (Function<?>) Ensemble.resolve(senderId);
        }
        return sender;
    }

    public ComputationBough getComputationBough() {
        return computationBough;
    }

    public long getExecutionId() {
        return getComputationBough().executionId;
    }
}
