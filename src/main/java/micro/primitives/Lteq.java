package micro.primitives;

import micro.Names;
import micro.Value;

import java.util.Map;

public class Lteq implements Primitive {

    public static Lteq lteq = new Lteq();

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(Map<String, Value> params) {
        return Library.lteq(As.Comparable(params, Names.left),(As.Comparable(params, Names.right)));
    }

    public static Primitive lteq() {
        return lteq;
    }
}
