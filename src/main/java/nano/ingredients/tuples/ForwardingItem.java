package nano.ingredients.tuples;

import nano.ingredients.Function;

public class ForwardingItem extends Tuple<KeyValuePair, Function<?>> {

    private ForwardingItem(KeyValuePair left, Function<?> right) {
        super(left, right);
    }

    public static ForwardingItem of(KeyValuePair left, Function<?> right){
        return new ForwardingItem(left,right);
    }

    public KeyValuePair keyValuePair(){
        return left;
    }

    public Function<?> target(){
        return right;
    }
}
