package miso.ingredients;

import java.util.Stack;
import java.util.stream.Collectors;

public class FunctionCallLevel {
    public final Origin origin;
    public final String matchString;

    public FunctionCallLevel(Origin origin) {
        this.origin = origin;
        matchString = origin.executionId.toString() + '/' +
                origin.callStack.stream()
                .map(Object::toString)
                .collect(Collectors.joining("/"));
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
        return matchString.equals(that.matchString);
    }

    @Override
    public int hashCode() {
        return matchString.hashCode();
    }
}
