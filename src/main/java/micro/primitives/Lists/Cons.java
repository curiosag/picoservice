package micro.primitives.Lists;

import micro.Check;
import micro.Names;
import micro.Value;
import micro.primitives.Primitive;

import java.util.ArrayList;
import java.util.List;

public class Cons implements Primitive {
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public Object execute(List<Value> params) {
        Object l = getValue(Names.list, params);
        Object e = getValue(Names.element, params);

        Check.invariant(l instanceof List, "List expected");
        Check.invariant(l != null, "Element expected");

        List list = (List) l;
        Check.invariant(list.isEmpty() || e.getClass().equals(list.get(0).getClass()), "can't add heterogenous element");
        ArrayList result = new ArrayList();
        result.add(e);
        result.addAll(list);
        return result;
    }
}
