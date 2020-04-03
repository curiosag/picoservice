package micro.primitives.Lists;

import micro.Check;
import micro.Names;
import micro.Value;
import micro.primitives.Primitive;

import java.util.List;

public class IsEmpty implements Primitive {

    @Override
    public Object execute(List<Value> params) {
        Object l = getValue(Names.list, params);
        Check.invariant(l instanceof List, "List expected");

        return ((List) l).isEmpty();
    }

    public static Primitive isEmpty(){
        return new IsEmpty();
    }
}
