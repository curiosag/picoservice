package miso.ingredients.tuples;

import miso.ingredients.Function;

public class OnReturnForwardItem extends Tuple<KeyValuePair, Function<?>> {

    private OnReturnForwardItem(KeyValuePair left, Function<?> right) {
        super(left, right);
    }

    public static OnReturnForwardItem of(KeyValuePair left, Function<?> right){
        return new OnReturnForwardItem(left,right);
    }

    public KeyValuePair keyValuePair(){
        return left;
    }

    public Function<?> target(){
        return right;
    }
}
