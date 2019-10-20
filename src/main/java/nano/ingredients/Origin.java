package nano.ingredients;

import nano.ingredients.tuples.ComputationBranch;
import nano.ingredients.tuples.ComputationOriginBranch;

import java.io.Serializable;
import java.util.Objects;

public class Origin implements Serializable {
    private static final long serialVersionUID = 0L;

    transient Function<?> sender; // not serialized, rehydrated by asking for Function for senderId
    public final String senderId;
    private final ComputationPath computationPath;
    final String prevFunctionCallId;
    final String lastFunctionCallId;

    public Origin(Function<?> sender, ComputationPath computationPath, String lastFunctionCallId, String prevFunctionCallId) {
        this.senderId = sender.address.id;
        this.sender = sender;
        this.computationPath = computationPath;
        this.prevFunctionCallId = prevFunctionCallId;
        this.lastFunctionCallId = lastFunctionCallId;
    }

    public Origin clearSenderRef(){
        Origin result = new Origin(sender, computationPath, lastFunctionCallId, prevFunctionCallId);
        result.sender = null;
        return result;
    }

    public static Origin origin(Function<?> sender) {
        String id = sender.address.id;
        return new Origin(sender, new ComputationPath(0L), id, "");
    }

    public static Origin origin(Function<?> sender, Long exid) {
        String id = sender.address.id;
        return new Origin(sender, new ComputationPath(exid), id, "");
    }

    public static Origin origin(Function<?> sender, ComputationPath computationPath, String prevFunctionCallId, String lastFunctionCallId) {
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
        ComputationPath path = computationPath.pop();
        return new Origin(getSender(), path, path.getLastPopped().id, lastFunctionCallId);
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
