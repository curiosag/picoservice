package nano.ingredients.tuples;

import nano.ingredients.Function;

public class ForwardingItem extends SerializableTuple<SerializableKeyValuePair, Function<?>> {

    private ForwardingItem(SerializableKeyValuePair left, Function<?> right) {
        super(left, right);
    }

    public static ForwardingItem of(SerializableKeyValuePair left, Function<?> right){
        return new ForwardingItem(left,right);
    }

    public SerializableKeyValuePair keyValuePair(){
        return left;
    }

    public Function<?> target(){
        return right;
    }

}
