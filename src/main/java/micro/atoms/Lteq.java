package micro.atoms;

import micro.Names;
import micro.Value;

import java.util.Map;

public class Lteq implements Primitive {

    @Override
    public Object execute(Map<String, Value> params) {
        return As.Comparable(params, Names.left).compareTo(As.Comparable(params, Names.right)) <= 0;
    }

    public static Lteq lteq(){
        return new Lteq();
    }
}
