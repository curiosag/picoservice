package nano.ingredients;

import java.io.Serializable;
import java.util.Objects;

public class FunctionCallTreeLocation implements Serializable {
    private static final long serialVersionUID = 0L;

    public final Origin origin;
    private final String stackString;

    FunctionCallTreeLocation(Origin origin) {
        this.origin = origin;
        this.stackString = origin.executionId + "/" + origin.getComputationBough().getStack().toString();
    }

    public Long getExecutionId() {
        return origin.executionId;
    }

    ComputationStackView getCallStack() {
        return origin.getComputationBough().getStack();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionCallTreeLocation that = (FunctionCallTreeLocation) o;
        return Objects.equals(this.origin.executionId, that.origin.executionId) &&
                Objects.equals(this.origin.getComputationBough().getStack(), that.origin.getComputationBough().getStack());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.origin.executionId, this.origin.getComputationBough().getStack());
    }

    @Override
    public String toString() {
        return stackString;
    }
}
