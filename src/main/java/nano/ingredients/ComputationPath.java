package nano.ingredients;

import nano.ingredients.guards.Guards;
import nano.ingredients.tuples.ComputationBranch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComputationPath implements Serializable {
    private static final long serialVersionUID = 0L;

    /*
     * a path of functionIds from root to a leaf of the execution tree.
     *
     * pushing: (1,3,7).push(3) = (1,3,7,3), topIndex = 3
     * pop: (1,3,7,3).pop() = (1,3,7,-3), topIndex = 2
     *
     * pushing again on a path where already something has been popped results in branching
     *
     *                        (1,3,7,-3), topIndex = 2
     * (1,3,7,-3).push(9)  = {
     *                        (1,3,7,9), topIndex = 3
     * */

    final long executionId;
    private final ArrayList<Long> functionCalls;
    private final Long lastPopped;
    private final int topIndex;
    private final Long sum;

    // only the positive elements of path, stack.size() == topIndex + 1;

    private transient ComputationStack stackView;
    public ComputationStack getStack() {
        if (stackView == null) {
            stackView = new ComputationStack(functionCalls, topIndex);
        }
        return stackView;
    }

    public ComputationPath(ComputationPath branchOffFrom) {
        this.executionId = branchOffFrom.executionId;
        topIndex = branchOffFrom.topIndex;
        functionCalls = branchOffFrom.getStack().getItems();
        lastPopped = null;
        sum = branchOffFrom.sum;
    }

    private static final ArrayList<Long> empty = new ArrayList<>();

    public ComputationPath(long executionId) {
        this.executionId = executionId;
        functionCalls = empty;
        lastPopped = null;
        topIndex = -1;
        sum = 0L;
    }
    private ComputationPath(ComputationPath current, Long toPush) {
        functionCalls = new ArrayList<>(current.functionCalls);
        this.executionId = current.executionId;
        Guards.isTrue(functionCalls.isEmpty() || functionCalls.get(functionCalls.size() - 1) >= 0);
        functionCalls.add(toPush);
        topIndex = current.topIndex + 1;
        lastPopped = current.lastPopped;
        sum = current.sum + toPush;
    }

    private ComputationPath(ComputationPath current, boolean pop) {
        this.executionId = current.executionId;
        Guards.isTrue(pop);
        Guards.isFalse(current.getStack().isEmpty());

        functionCalls = new ArrayList<>(current.functionCalls);
        lastPopped = functionCalls.get(current.topIndex);
        functionCalls.remove(current.topIndex);
        functionCalls.add(current.topIndex, lastPopped * -1);
        topIndex = current.topIndex - 1;
        sum = current.sum + lastPopped;
    }

    public Long getSum() {
        return sum;
    }

    Long getLastPopped() {
        return lastPopped;
    }

    ComputationBranch push(Long functionId) {
        if (nothingPoppedYet()) {
            return ComputationBranch.of(new ComputationPath(this, functionId), Optional.empty());
        } else {
            // TODO could be a bit less of, eh, ...
            ComputationPath branchedOff = new ComputationPath(new ComputationPath(this), functionId);
            return ComputationBranch.of(branchedOff, Optional.of(this));
        }
    }

    private boolean nothingPoppedYet() {
        return topIndex + 1 == functionCalls.size();
    }

    ComputationPath pop() {
        Guards.isTrue(topIndex >= 0);
        return new ComputationPath(this, true);
    }

    @Override
    public String toString() {
        return functionCalls.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("/"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputationPath that = (ComputationPath) o;
        return  executionId == that.executionId &&
                topIndex == that.topIndex &&
                functionCalls.equals(that.functionCalls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId, functionCalls, topIndex);
    }

    public boolean isEmpty() {
        return functionCalls.isEmpty();
    }

    public int size() {
        return functionCalls.size();
    }

    public List<Long> items() {
        return functionCalls;
    }
}
