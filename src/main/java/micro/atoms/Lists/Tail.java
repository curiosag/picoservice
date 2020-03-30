package micro.atoms.Lists;

import micro.Check;
import micro.Names;
import micro.Value;
import micro.atoms.Primitive;

import java.util.List;
import java.util.Map;

public class Tail implements Primitive {

    @Override
    public Object execute(Map<String, Value> params) {
        Value param = params.get(Names.list);
        Check.invariant(param != null && param.get() instanceof List, "List expected");

        List list = (List) param.get();
        Check.invariant(!list.isEmpty(), "list is empty");

        return list.subList(1, list.size());
    }
}
