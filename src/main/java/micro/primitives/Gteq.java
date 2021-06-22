package micro.primitives;

import micro.Names;
import micro.Value;

import java.util.Map;

public class Gteq implements Primitive {

    public static Gteq gteq = new Gteq();

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(Map<String, Value> params) {
        return Library.gteq(As.Comparable(params, Names.left),(As.Comparable(params, Names.right)));
    }

    public static Gteq gteq(){
        return gteq;
    }

}
