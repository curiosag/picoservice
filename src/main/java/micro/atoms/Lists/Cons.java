package micro.atoms.Lists;

import micro.Check;
import micro.Names;
import micro.Value;
import micro.atoms.Primitive;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cons implements Primitive {
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public Object execute(Map<String, Value> params) {
        Value vl = params.get(Names.list);
        Value ve = params.get(Names.element);

        Check.invariant(vl != null, "List expected");
        Check.invariant(ve != null, "Element expected");

        Object l = vl.get();
        Object e = ve.get();

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
