package miso.ingredients;

import miso.ingredients.guards.Guards;

import java.util.Objects;
import java.util.Stack;

public class Origin {
    public final Function<?> sender;
    public final Long executionId;
    public final Long seqNr;
    public final Stack<Integer> callStack = new Stack<>();
    public final Integer lastPopped;

    public Origin pushCall(Integer id) {
        Origin result = new Origin(sender, executionId, seqNr, callStack, lastPopped);
        result.callStack.push(id);
        return result;
    }

    public Origin popCall() {
        Guards.notEmpty(callStack);
        return new Origin(sender, executionId, seqNr, callStack, callStack.pop());
    }

    private Origin(Function<?> sender, Long executionId, Long seqNr, Stack<Integer> callStack, Integer lastPopped) {
        this.sender = sender;
        this.executionId = executionId;
        this.seqNr = seqNr;
        this.callStack.addAll(callStack);
        this.lastPopped = lastPopped;
    }

    public static Origin origin(Function<?> sender) {
        return new Origin(sender,  0L, 0L, new Stack<>(), null);
    }

    public static Origin origin(Function<?> sender, Long executionId, Long seqNr, Stack<Integer> callStack) {
        return new Origin(sender, executionId, seqNr, callStack, null);
    }

    Origin sender(Function<?> sender) {
        return origin(sender, executionId, seqNr + 1L, callStack);
    }

    @Override
    public String toString() {
        //TODO
        return "";
        // return String.format("Origin:(%d/%d)%s->%s (%d states left)", executionId, callLevel, scope.address, sender.address, sender.executionStates.size());
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

    private FunctionCallLevel functionCallLevel;
    public FunctionCallLevel functionCallLevel(){
        if (functionCallLevel == null)
        {
            functionCallLevel = new FunctionCallLevel(this);
        }
        return functionCallLevel;
    }
}
