package nano.ingredients.tuples;

import java.io.Serializable;

public class SerializableTuple<U extends Serializable,V extends Serializable> extends Tuple <U, V> implements Serializable {
    public SerializableTuple(U left, V right) {
        super(left, right);
    }

    public static <A extends Serializable,B extends Serializable> SerializableTuple<A,B> of(A u, B v){
        return new SerializableTuple<>(u, v);
    }
}
