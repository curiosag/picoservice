package miso.ingredients;

import miso.ingredients.guards.Guards;

import java.util.Objects;
import java.util.Stack;

import static miso.ingredients.Nop.nop;

public class Origin {
    public final Function<?> sender;
    public final Function<?> triggeredBy;
    public final Long executionId;
    public final Long seqNr;
    public final Stack<Integer> callStack = new Stack<>();

    public void pushCall(Integer id) {
        callStack.push(id);
    }

    public Integer popCall() {
        Guards.notEmpty(callStack);
        return callStack.pop();
    }

    private Origin(Function<?> sender, Function<?> triggeredBy, Long executionId, Long seqNr, Stack<Integer> callStack) {
        this.sender = sender;
        this.triggeredBy = triggeredBy;
        this.executionId = executionId;
        this.seqNr = seqNr;
        this.callStack.addAll(callStack);
    }

    public static Origin origin(Function<?> sender) {
        return new Origin(sender, nop,  0L, 0L, new Stack<>());
    }

    public static Origin origin(Function<?> sender, Function<?> scope, Long executionId, Long seqNr, Stack<Integer> callStack) {
        return new Origin(sender, scope, executionId, seqNr, callStack);
    }

    Origin sender(Function<?> sender) {
        return origin(sender, triggeredBy, executionId, seqNr + 1L, callStack);
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
                Objects.equals(triggeredBy, origin.triggeredBy) &&
                Objects.equals(executionId, origin.executionId) &&
                Objects.equals(callStack, origin.callStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, triggeredBy, executionId, callStack);
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
