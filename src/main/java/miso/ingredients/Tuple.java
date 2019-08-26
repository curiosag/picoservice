package miso.ingredients;

import java.util.Objects;

public class Tuple<U,V> {
    public  U left;
    public  V right;

    public Tuple(U left, V right) {
        this.left = left;
        this.right = right;
    }

    public static <A,B> Tuple<A,B> of(A u, B v){
        return new Tuple<>(u, v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(left, tuple.left) &&
                Objects.equals(right, tuple.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
