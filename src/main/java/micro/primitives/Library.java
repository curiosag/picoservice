package micro.primitives;

import java.util.ArrayList;
import java.util.List;

@Lib
public class Library {

    public static <T> boolean empty(List<T> values) {
        return values.isEmpty();
    }

    public static <T> T head(List<T> values) {
        return values.get(0);
    }

    public static <T> List<T> tail(List<T> values) {
        return values.subList(1, values.size());
    }

    public static <T> List<T> cons(T value, List<T> values) {
        ArrayList<T> result = new ArrayList<>();
        result.add(value);
        result.addAll(values);
        return result;
    }

    public static <T> List<T> concat(List<T> l1, List<T> l2) {
        ArrayList<T> result = new ArrayList<>();
        result.addAll(l1);
        result.addAll(l2);
        return result;
    }

    /**
     * a > b ?
     */
    public static <T extends Comparable<T>> Boolean gt(T a, T b) {
        return a.compareTo(b) > 0;
    }

    /**
     * a < b ?
     */
    public static <T extends Comparable<T>> Boolean lt(T a, T b) {
        return a.compareTo(b) < 0;
    }

    /**
     * a = b ?
     */
    public static <T extends Comparable<T>> Boolean eq(T a, T b) {
        return a.compareTo(b) == 0;
    }

    /**
     * a <= b ?
     */
    public static <T extends Comparable<T>> Boolean lteq(T a, T b) {
        return a.compareTo(b) <= 0;
    }


    /**
     * a >= b ?
     */
    public static <T extends Comparable<T>> Boolean gteq(T a, T b) {
        return a.compareTo(b) >= 0;
    }
}
