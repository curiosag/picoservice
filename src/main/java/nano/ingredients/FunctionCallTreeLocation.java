package nano.ingredients;

import java.util.Objects;

public class FunctionCallTreeLocation {
    public final Origin origin;
    private final String stackString;

    public FunctionCallTreeLocation(Origin origin) {
        this.origin = origin;
        this.stackString = origin.executionId.toString() + '/' + origin.callStack.toString();
    }

    public Long getExecutionId() {
        return origin.executionId;
    }

    public CallStack getCallStack() {
        return origin.callStack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionCallTreeLocation that = (FunctionCallTreeLocation) o;
        return Objects.equals(this.origin.executionId, that.origin.executionId) &&
                Objects.equals(this.origin.callStack, that.origin.callStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.origin.executionId, this.origin.callStack);
    }

    @Override
    public String toString() {
        return stackString;
    }
}
