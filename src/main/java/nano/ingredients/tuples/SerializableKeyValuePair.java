package nano.ingredients.tuples;

import java.io.Serializable;

public class SerializableKeyValuePair extends SerializableTuple<String, Serializable> {

    private SerializableKeyValuePair(String left, Serializable right) {
        super(left, right);
    }

    public static SerializableKeyValuePair of(String left, Serializable right){
        return new SerializableKeyValuePair(left,right);
    }

    public String key(){
        return left;
    }

    public Serializable value(){
        return right;
    }
}
