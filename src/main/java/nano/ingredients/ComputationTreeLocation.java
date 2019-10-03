package nano.ingredients;

import java.io.Serializable;
import java.util.Objects;

public class ComputationTreeLocation implements Serializable {
    private static final long serialVersionUID = 0L;

    public final Origin origin;
    private final String stackString;

    ComputationTreeLocation(Origin origin) {
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
        ComputationTreeLocation that = (ComputationTreeLocation) o;
        return stackString.equals(that.stackString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stackString);
    }

    @Override
    public String toString() {
        return stackString;
    }
}
