package nano.ingredients;

import java.io.Serializable;
import java.util.Objects;

public class Origin implements Serializable {
    private static final long serialVersionUID = 0L;

    transient Function<?> sender;
    public final long senderId;
    public final long executionId;
    public CallStack callStack;
    public final long prevFunctionCallId;
    public final long lastFunctionCallId;

    Origin(Function<?> sender, long executionId, CallStack callStack, long lastFunctionCallId, long prevFunctionCallId) {
        this.senderId = sender.address.id;
        this.sender = sender;
        this.executionId = executionId;
        this.callStack = callStack;
        this.prevFunctionCallId = prevFunctionCallId;
        this.lastFunctionCallId = lastFunctionCallId;
    }

    public static Origin origin(Function<?> sender) {
        return new Origin(sender, 0L, new CallStack(), sender.address.id, -1L);
    }

    public static Origin origin(Function<?> sender, long executionId, CallStack callStack, long prevFunctionCallId, long lastFunctionCallId) {
        return new Origin(sender, executionId, callStack, prevFunctionCallId, lastFunctionCallId);
    }

    Origin sender(Function<?> sender) {
        return origin(sender, executionId, callStack, prevFunctionCallId, lastFunctionCallId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Origin)) {
            throw new IllegalStateException();
        }
        Origin origin = (Origin) o;
        return senderId == origin.senderId && // sender isn't necessarily in the call stack, can be something which is not a FunctionCall
                executionId == origin.executionId &&
                callStack.equals(origin.callStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSender(), executionId, callStack);
    }

    private FunctionCallTreeLocation functionCallTreeLocation;

    public FunctionCallTreeLocation callTreePath() {
        if (functionCallTreeLocation == null) {
            functionCallTreeLocation = new FunctionCallTreeLocation(this);
        }
        return functionCallTreeLocation;
    }

    Origin popCall() {
        CallStack stack = callStack.pop();
        return new Origin(getSender(), executionId, stack, stack.getLastPopped(), lastFunctionCallId);
    }

    Origin pushCall(FunctionCall functionCall) {
        CallStack stack = callStack.push(functionCall.address.id);
        return new Origin(getSender(), executionId, stack, functionCall.address.id, lastFunctionCallId);
    }

    public Function<?> getSender() {
        if (sender == null) {
            sender = (Function<?>) Ensemble.resolve(senderId);
        }
        return sender;
    }
}
