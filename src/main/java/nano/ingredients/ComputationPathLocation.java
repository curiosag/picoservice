package nano.ingredients;

import java.io.Serializable;
import java.util.Objects;

public class ComputationPathLocation implements Serializable {
    private static final long serialVersionUID = 0L;

    public final Origin origin;
    private final String stackString;

    ComputationPathLocation(Origin origin) {
        this.origin = origin;
        this.stackString = origin.getExecutionId() + "/" + origin.getComputationPath().getStack().toString();
    }

    public Long getExecutionId() {
        return origin.getExecutionId();
    }

    ComputationStack getCallStack() {
        return origin.getComputationPath().getStack();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputationPathLocation that = (ComputationPathLocation) o;
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
