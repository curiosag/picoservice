package miso.ingredients.tuples;

public class KeyValuePair extends Tuple<String, Object> {

    private KeyValuePair(String left, Object right) {
        super(left, right);
    }

    public static KeyValuePair of(String left, Object right){
        return new KeyValuePair(left,right);
    }

    public String key(){
        return left;
    }

    public Object value(){
        return right;
    }
}
