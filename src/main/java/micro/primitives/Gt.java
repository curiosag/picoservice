package micro.primitives;

import micro.Names;
import micro.Value;

import java.util.Map;

public class Gt implements Primitive {

    public static Gt gt = new Gt();

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(Map<String, Value> params) {
        return Library.gt(As.Comparable(params, Names.left), As.Comparable(params, Names.right));
    }

    public static Primitive gt() {
        return gt;
    }
}
