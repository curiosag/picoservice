package nano.ingredients;

import java.util.Objects;

class CallStackItem {

    public final int functionId;
    public int recalls = 1;

    /*
     *   say function f has id 1
     *
     *   a single call gives you an Item (1,1)
     *   another recursive call gives you (1,2)
     *   when the recursive call returns we pop, giving again (1,1)
     *   when the initial call terminates the Item gets removed from the stack
     *
     *   Indirect recursion will prevent this kind of optimization
     *
     * */
    CallStackItem(int functionId) {
        this.functionId = functionId;
    }

    CallStackItem(int functionId, int recalls) {
        this.functionId = functionId;
        this.recalls = recalls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallStackItem callStackItem = (CallStackItem) o;
        return functionId == callStackItem.functionId &&
                recalls == callStackItem.recalls;
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionId, recalls);
    }
}
