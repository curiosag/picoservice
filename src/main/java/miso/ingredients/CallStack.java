package miso.ingredients;

import miso.ingredients.guards.Guards;

import java.util.ListIterator;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;

public class CallStack {
    private final Stack<CallStackItem> stack = new Stack<>();

    public int size() {
        return size;
    }

    private int size = 0;


    public CallStack() {
    }

    public CallStack(CallStack takeFrom) {
        addAll(takeFrom);
    }

    //TODO: make callstack immutable
    public void push(int functionId) {
        size++;
        if (stack.isEmpty() || stack.peek().functionId != functionId) {
            stack.push(new CallStackItem(functionId));
        } else {
            stack.peek().recalls++;
        }
    }

    public int pop() {
        Guards.isFalse(stack.isEmpty());
        size--;
        CallStackItem item = stack.peek();
        int result = item.functionId;
        if (item.recalls > 1) {
            item.recalls--;
        } else {
            stack.pop();
        }
        return result;
    }

    public void addAll(CallStack other) {
        //concurrent modifications on other may happen
        for (ListIterator<CallStackItem> it = other.stack.listIterator(); it.hasNext(); ) {
            CallStackItem item = it.next();
            stack.add(new CallStackItem(item.functionId, item.recalls));
        }
        size = stack.stream().map(i -> i.recalls).reduce(0, (i, j) -> i + j);
    }

    @Override
    public String toString() {
        CallStack calcFrom = new CallStack();
        calcFrom.addAll(this);
        return calcFrom.stack.stream()
                .map(i -> {
                    if (i.recalls == 1) {
                        return String.valueOf(i.functionId);
                    } else {
                        return String.format("%d(%d)", i.functionId, i.recalls);
                    }
                })
                .collect(Collectors.joining("/"));
    }

    public boolean startsWith(CallStack that) {
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

        if (!(thisItem.functionId == thatItem.functionId && thisItem.recalls >= thatItem.recalls)) {
            return false;
        }

        for (int i = that.stack.size() - 1; i > 0 ; i--) {
            thisItem = this.stack.get(i);
            thatItem = that.stack.get(i);
            if (!(thisItem.functionId == thatItem.functionId && thisItem.recalls == thatItem.recalls)) {
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
