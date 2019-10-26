package nano.ingredients;

import java.util.Objects;

public class ComputationNode {
    public final String id;

    public final boolean callReturned;

    public ComputationNode(String id) {
        this.id = id;this.callReturned = false;
    }
    public ComputationNode(String id, boolean callReturned) {
        this.id = id;
        this.callReturned = callReturned;
    }

    public ComputationNode withCallReturned() {
        return new ComputationNode(id, true);
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputationNode that = (ComputationNode) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
