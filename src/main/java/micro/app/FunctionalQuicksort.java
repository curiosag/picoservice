package micro.app;

import java.util.List;
import java.util.function.Function;

import static micro.primitives.Library.*;

public class FunctionalQuicksort {

    public static <T extends Comparable<T>> List<T> filter(List<T> values, Function<T, Boolean> predicate) {
        if (empty(values)) {
            return values;
        } else {
            T head = head(values);
            List<T> tail = filter(tail(values), predicate);
            if (predicate.apply(head)) {
                return cons(head, tail);
            } else return tail;
        }
    }

    public static <T extends Comparable<T>> List<T> qsort(List<T> list) {
        if (empty(list))
            return list;
        else {
            var head = head(list);
            var tail = tail(list);
            var left = filter(tail, i -> lt(i, head));
            var right = filter(tail, i -> gt(i, head));
            return concat(qsort(left), cons(head, qsort(right)));
        }
    }

    public static void main(String[] args) {
        System.out.println(qsort(List.of(5,1,2,4,3)));
    }

}
