package miso.ingredients;

import java.util.Objects;
import java.util.Stack;

public class FunctionCallLevel {
    Origin origin;
    public FunctionCallLevel(Origin origin) {
        this.origin = origin;
    }

    public Long getExecutionId(){
        return origin.executionId;
    }

    public Stack<Integer> getCallStack(){
        return origin.callStack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionCallLevel that = (FunctionCallLevel) o;
        return Objects.equals(getExecutionId(), that.getExecutionId()) &&
                Objects.equals(getCallStack(), that.getCallStack());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExecutionId(), getCallStack());
    }
}
