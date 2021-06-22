package micro.primitives;

import micro.Names;
import micro.Value;

import java.util.Map;

public class Lt implements Primitive {

    public static Lt lt = new Lt();

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(Map<String, Value> params) {
        return Library.lt(As.Comparable(params, Names.left), As.Comparable(params, Names.right));
    }

    public static Primitive lt() {
        return lt;
    }

}
