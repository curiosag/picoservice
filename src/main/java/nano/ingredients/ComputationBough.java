package nano.ingredients;

import nano.ingredients.guards.Guards;
import nano.ingredients.tuples.ComputationBoughBranch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComputationBough implements Serializable {
    private static final long serialVersionUID = 0L;

    /*
     * a bough (a branch without the notion of branching) of functionIds from root to a leaf of the
     * execution tree. Also resonates with the title of a strange book, "The Golden Baugh". And if anybody's interested
     * in that, then Julian Jayne's "The Origin of Consciousness in The Breakdown of The Bicameral Mind" should not be
     * avoided.
     *
     * during pushing it just grows, the topIndex gets increased by 1. e.g (1,3,7).push(3) = (1,3,7,3), topIndex = 3
     * popping inverts items and decreases the topIndex. e.g. (1,3,7,3).pop() = (1,3,7,-3), topIndex = 2
     *
     * */

    public final long executionId;
    private final ArrayList<Long> functionCalls;
    private final Long lastPopped;
    private final int topIndex;

    // only the positive elements of bough, stack.size() == topIndex + 1;
    private transient ComputationStack stackView;

    public ComputationStack getStack() {
        if (stackView == null) {
            stackView = new ComputationStack(functionCalls, topIndex);
        }
        return stackView;
    }

    public ComputationBough(ComputationBough branchOffFrom) {
        this.executionId = branchOffFrom.executionId;
        topIndex = branchOffFrom.topIndex;
        functionCalls = branchOffFrom.getStack().getItems();
        lastPopped = null;
    }

    private static final ArrayList<Long> emptyBough = new ArrayList<>();
    public ComputationBough(long executionId) {
        this.executionId = executionId;
        functionCalls = emptyBough;
        lastPopped = null;
        topIndex = -1;
    }

    private ComputationBough(ComputationBough current, Long toPush) {
        functionCalls = new ArrayList<>(current.functionCalls);
        this.executionId = current.executionId;
        Guards.isTrue(functionCalls.isEmpty() || functionCalls.get(functionCalls.size() - 1) >= 0);
        functionCalls.add(toPush);
        topIndex = current.topIndex + 1;
        lastPopped = current.lastPopped;
    }

    private ComputationBough(ComputationBough current, boolean pop) {
        this.executionId = current.executionId;
        Guards.isTrue(pop);
        Guards.isFalse(current.getStack().isEmpty());

        functionCalls = new ArrayList<>(current.functionCalls);
        lastPopped = functionCalls.get(current.topIndex);
        functionCalls.remove(current.topIndex);
        functionCalls.add(current.topIndex, lastPopped * -1);
        topIndex = current.topIndex - 1;
    }

    Long getLastPopped() {
        return lastPopped;
    }

    ComputationBoughBranch push(Long functionId) {
        if (nothingPoppedYet()) {
            return ComputationBoughBranch.of(new ComputationBough(this, functionId), Optional.empty());
        } else {
            // TODO could be a bit less of, eh, ...
            ComputationBough branchedOff = new ComputationBough(new ComputationBough(this), functionId);
            return ComputationBoughBranch.of(branchedOff, Optional.of(this));
        }
    }

    private boolean nothingPoppedYet() {
        return topIndex + 1 == functionCalls.size();
    }

    ComputationBough pop() {
        Guards.isTrue(topIndex >= 0);
        return new ComputationBough(this, true);
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
        ComputationBough that = (ComputationBough) o;
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

}
