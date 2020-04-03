package micro.primitives.Lists;

import micro.Check;
import micro.Names;
import micro.Value;
import micro.primitives.Primitive;

import java.util.List;

public class Tail implements Primitive {

    @Override
    public Object execute(List<Value> params) {
        Object l = getValue(Names.list, params);
        Check.invariant(l instanceof List, "List expected");

        List list = (List) l;
        Check.invariant(!list.isEmpty(), "list is empty");
        return list.subList(1, list.size());
    }
}
