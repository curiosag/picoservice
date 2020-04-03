package micro.primitives.Lists;

import micro.Check;
import micro.Names;
import micro.Value;
import micro.primitives.Primitive;

import java.util.ArrayList;
import java.util.List;

public class Concat implements Primitive {

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public Object execute(List<Value> params) {
        Object l = getValue(Names.left, params);
        Object r = getValue(Names.right, params);
        Check.invariant(l instanceof List && r instanceof List, "Lists expected");

        ArrayList result = new ArrayList();
        result.addAll((List)l);
        result.addAll((List)r);
        return result;
    }
}
