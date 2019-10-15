package nano.ingredients;

import nano.ingredients.tuples.ComputationBranch;
import nano.ingredients.tuples.ComputationOriginBranch;

import java.io.Serializable;
import java.util.Objects;

public class Origin implements Serializable {
    private static final long serialVersionUID = 0L;

    transient Function<?> sender; // not serialized, rehydrated by asking for Function for senderId
    public final long senderId;
    private final ComputationPath computationPath;
    final long prevFunctionCallId;
    final long lastFunctionCallId;

    Origin(Function<?> sender, ComputationPath computationPath, long lastFunctionCallId, long prevFunctionCallId) {
        this.senderId = sender.address.id;
        this.sender = sender;
        this.computationPath = computationPath;
        this.prevFunctionCallId = prevFunctionCallId;
        this.lastFunctionCallId = lastFunctionCallId;
    }

    public static Origin origin(Function<?> sender) {
        Long id = sender.address.id;
        return new Origin(sender, new ComputationPath(0L), id, -1L);
    }

    public static Origin origin(Function<?> sender, Long exid) {
        Long id = sender.address.id;
        return new Origin(sender, new ComputationPath(exid), id, -1L);
    }

    public static Origin origin(Function<?> sender, ComputationPath computationPath, long prevFunctionCallId, long lastFunctionCallId) {
        return new Origin(sender, computationPath, prevFunctionCallId, lastFunctionCallId);
    }

    Origin sender(Function<?> sender) {
        return origin(sender, computationPath, prevFunctionCallId, lastFunctionCallId);
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
                computationPathLocation().getCallStack().equals(origin.computationPathLocation().getCallStack());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSender(), getExecutionId(), computationPath);
    }

    private ComputationPathLocation computationPathLocation;

    public ComputationPathLocation computationPathLocation() {
        if (computationPathLocation == null) {
            computationPathLocation = new ComputationPathLocation(this);
        }
        return computationPathLocation;
    }

    Origin popCall() {
        ComputationPath bough = computationPath.pop();
        return new Origin(getSender(), bough, bough.getLastPopped(), lastFunctionCallId);
    }

    ComputationOriginBranch pushCall(FunctionCall functionCall) {
        ComputationBranch maybeBranch = computationPath.push(functionCall.address.id);

        Origin o = new Origin(getSender(), maybeBranch.getExecutionPath(), functionCall.address.id, lastFunctionCallId);
        return ComputationOriginBranch.of(o, maybeBranch.getPathBranchedOffFrom());
    }

    public Function<?> getSender() {
        if (sender == null) {
            sender = (Function<?>) Ensemble.resolve(senderId);
        }
        return sender;
    }

    public ComputationPath getComputationPath() {
        return computationPath;
    }

    public long getExecutionId() {
        return getComputationPath().executionId;
    }
}
