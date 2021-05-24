package micro.primitives.Lists;

import micro.Check;
import micro.Names;
import micro.Value;
import micro.primitives.Library;
import micro.primitives.Primitive;

import java.util.List;
import java.util.Map;

public class Concat implements Primitive {

    @SuppressWarnings({"unchecked", "ConstantConditions, rawtypes"})
    @Override
    public Object execute(Map<String, Value> params) {
        Value vl = params.get(Names.left);
        Value vr = params.get(Names.right);

        Check.invariant(vl != null && vr != null, "Lists left/right expected");

        Object l = vl.get();
        Object r = vr.get();
        Check.invariant(l instanceof List && r instanceof List, "Lists expected");

        return Library.concat((List)vl.get(), (List)vr.get());
    }
}
