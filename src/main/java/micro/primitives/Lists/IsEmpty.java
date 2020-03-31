package micro.primitives.Lists;

import micro.Check;
import micro.Names;
import micro.Value;
import micro.primitives.Primitive;

import java.util.List;
import java.util.Map;

public class IsEmpty implements Primitive {

    @Override
    public Object execute(Map<String, Value> params) {
        Value param = params.get(Names.list);
        Check.invariant(param != null && param.get() instanceof List, "List expected");

        return ((List) param.get()).isEmpty();
    }

    public static Primitive isEmpty(){
        return new IsEmpty();
    }
}
