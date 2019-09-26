package miso.ingredients;

import java.util.Objects;

public class Origin {
    public final Function<?> sender;
    public final Long executionId;
    public final Long seqNr;
    public final CallStack callStack;

    Origin(Function<?> sender, Long executionId, Long seqNr, CallStack callStack) {
        this.sender = sender;
        this.executionId = executionId;
        this.seqNr = seqNr;
        this.callStack = callStack;
    }

    public static Origin origin(Function<?> sender) {
        return new Origin(sender, 0L, 0L, new CallStack());
    }

    public static Origin origin(Function<?> sender, Long executionId, Long seqNr, CallStack callStack) {
        return new Origin(sender, executionId, seqNr, callStack);
    }

    Origin sender(Function<?> sender) {
        return origin(sender, executionId, seqNr + 1L, callStack);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Origin origin = (Origin) o;
        return Objects.equals(sender, origin.sender) &&
                Objects.equals(executionId, origin.executionId) &&
                Objects.equals(callStack, origin.callStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, executionId, callStack);
    }

    private FunctionCallTreeLocation functionCallTreeLocation;

    public FunctionCallTreeLocation functionCallTreeNode() {
        if (functionCallTreeLocation == null) {
            functionCallTreeLocation = new FunctionCallTreeLocation(this);
        }
        return functionCallTreeLocation;
    }

    Origin popCall() {
        CallStack stack = callStack.pop();
        return new Origin(sender, executionId, seqNr, stack);
    }

    Origin pushCall(FunctionCall functionCall) {
        CallStack stack = callStack.push(functionCall.address.id);
        return new Origin(sender, executionId, seqNr, stack);
    }
}
