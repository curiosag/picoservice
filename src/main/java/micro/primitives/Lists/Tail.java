package micro.primitives.Lists;

import micro.Check;
import micro.Names;
import micro.Value;
import micro.primitives.Library;
import micro.primitives.Primitive;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "ConstantConditions", "rawtypes"})
public class Tail implements Primitive {

    public static Tail tail = new Tail();

    @Override
    public Object execute(Map<String, Value> params) {
        Value param = params.get(Names.list);
        Check.invariant(param != null && param.get() instanceof List, "List expected");

        List list = (List) param.get();
        Check.invariant(!list.isEmpty(), "list is empty");

        return Library.tail(list);
    }

}
