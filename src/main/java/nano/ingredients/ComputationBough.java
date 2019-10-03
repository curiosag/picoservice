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

    private final ArrayList<Long> bough;
    private final Long lastPopped;
    private final int topIndex;

    // only the positive elements of bough, stack.size() == topIndex + 1;
    private transient ComputationStackView stackView;

    public ComputationStackView getStack() {
        if (stackView == null) {
            stackView = new ComputationStackView(bough, topIndex);
        }
        return stackView;
    }

    public ComputationBough(ComputationBough branchOffFrom) {
        topIndex = branchOffFrom.topIndex;
        bough = branchOffFrom.getStack().getItems();
        lastPopped = null;
    }

    private static final ArrayList<Long> emptyBough = new ArrayList<>();
    public ComputationBough() {
        bough = emptyBough;
        lastPopped = null;
        topIndex = -1;
    }

    private ComputationBough(ComputationBough current, Long toPush) {
        bough = new ArrayList<>(current.bough);
        Guards.isTrue(bough.isEmpty() || bough.get(bough.size() - 1) >= 0);
        bough.add(toPush);
        topIndex = current.topIndex + 1;
        lastPopped = current.lastPopped;
    }

    private ComputationBough(ComputationBough current, boolean pop) {
        Guards.isTrue(pop);
        Guards.isFalse(current.getStack().isEmpty());

        bough = new ArrayList<>(current.bough);
        lastPopped = bough.get(current.topIndex);
        bough.remove(current.topIndex);
        bough.add(current.topIndex, lastPopped * -1);
        topIndex = current.topIndex - 1;
    }

    Long getLastPopped() {
        return lastPopped;
    }

    ComputationBoughBranch push(Long functionId) {
        if (nothingPoppedYet()) {
            return ComputationBoughBranch.of(new ComputationBough(this, functionId), Optional.empty());
        } else {
            // TODO could be a bit less, eh, ...
            ComputationBough branchedOff = new ComputationBough(new ComputationBough(this), functionId);
            return ComputationBoughBranch.of(branchedOff, Optional.of(this));
        }
    }

    private boolean nothingPoppedYet() {
        return topIndex + 1 == bough.size();
    }

    ComputationBough pop() {
        Guards.isTrue(topIndex >= 0);
        return new ComputationBough(this, true);
    }

    @Override
    public String toString() {
        return bough.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("/"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputationBough that = (ComputationBough) o;
        return topIndex == that.topIndex &&
                bough.equals(that.bough);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bough, topIndex);
    }

    public boolean isEmpty() {
        return bough.isEmpty();
    }

    public int size() {
        return bough.size();
    }

}
