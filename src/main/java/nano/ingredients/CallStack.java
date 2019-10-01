package nano.ingredients;

import nano.ingredients.guards.Guards;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class CallStack implements Serializable {
    private static final long serialVersionUID = 0L;

    public final Stack<CallStackItem> stack = new Stack<>();

    public int size() {
        return size;
    }

    private int size = 0;

    private Long lastPopped;

    public CallStack() {
    }

    private static Map<Integer, String> points = new HashMap<>();

    static String points(CallStack s) {
        String result = points.get(s.size);
        if (result == null) {
            result = String.format("%3d %s", s.size, String.join("", Collections.nCopies(s.size, "*")));
            points.put(s.size, result);
        }
        return result;
    }

    private CallStack(CallStack other) {
        lastPopped = other.lastPopped;
        for (CallStackItem item : other.stack) {
            stack.add(new CallStackItem(item.functionId, item.recalls));
        }
        size = stack.stream().map(i -> i.recalls).reduce(0, (i, j) -> i + j);
    }


    Long getLastPopped() {
        return lastPopped;
    }

    public CallStack push(Long functionId) {
        return new CallStack(this).pushInternal(functionId);
    }

    private CallStack pushInternal(Long functionId) {
        size++;
        if (stack.isEmpty() || !stack.peek().functionId.equals(functionId)) {
            stack.push(new CallStackItem(functionId));
        } else {
            stack.peek().recalls++;
        }
        return this;
    }

    CallStack pop() {
        return new CallStack(this).popInternal();
    }

    private CallStack popInternal() {
        Guards.isFalse(stack.isEmpty());
        size--;
        CallStackItem item = stack.peek();
        lastPopped = item.functionId;
        if (item.recalls > 1) {
            item.recalls--;
        } else {
            stack.pop();
        }

        return this;
    }


    @Override
    public String toString() {
        return stack.stream()
                .map(i -> {
                    if (i.recalls == 1) {
                        return String.valueOf(i.functionId);
                    } else {
                        return String.format("%d(%d)", i.functionId, i.recalls);
                    }
                })
                .collect(Collectors.joining("/"));
    }

    boolean startsWith(CallStack that) {
        if (this.stack.size() < that.stack.size()) {
            return false;
        }
        if (that.stack.size() == 0) {
            return true;
        }
        CallStackItem thisItem;
        CallStackItem thatItem;
        thisItem = this.stack.get(that.stack.size() - 1);
        thatItem = that.stack.get(that.stack.size() - 1);

        if (!(thisItem.functionId.equals(thatItem.functionId) && thisItem.recalls >= thatItem.recalls)) {
            return false;
        }

        for (int i = that.stack.size() - 1; i > 0; i--) {
            thisItem = this.stack.get(i);
            thatItem = that.stack.get(i);
            if (!(thisItem.functionId.equals(thatItem.functionId) && thisItem.recalls == thatItem.recalls)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallStack callStack = (CallStack) o;
        return Objects.equals(stack, callStack.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
