package nano.ingredients;

import java.util.*;
import java.util.stream.Collectors;

public class ComputationStackView {

    private final List<Long> stack;

    private static Map<Integer, String> points = new HashMap<>();

    public ArrayList<Long> getItems(){
        return new ArrayList<>(stack);
    }

    public int size() {
        return stack.size();
    }

    private Long get(int i) {
        return stack.get(i);
    }

    String stackPoints() {
        int size = Math.toIntExact(stack.stream().filter(i -> i > 0).count());
        String result = points.get(size);
        if (result == null) {
            result = String.format("%3d %s", size, String.join("", Collections.nCopies(size, "*")));
            points.put(size, result);
        }
        return result;
    }

    ComputationStackView(ArrayList<Long> items) {
        stack = items;
    }

    public ComputationStackView(ArrayList<Long> bough, int topIndex) {
        stack = new ArrayList<>(bough.subList(0, topIndex + 1));
    }

    @Override
    public String toString() {
        return stack.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("/"));
    }

    boolean startsWith(ComputationStackView that) {
        if (this.size() < that.size()) {
            return false;
        }

        for (int i = that.size() - 1; i > 0; i--) {
            if (!this.get(i).equals(that.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputationStackView that = (ComputationStackView) o;
        return stack.equals(that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
