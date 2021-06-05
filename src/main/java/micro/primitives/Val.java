package micro.primitives;

import micro.Check;
import micro.Value;

import java.util.Map;

public class Val implements Primitive {
    @Override
    public Object execute(Map<String, Value> parameters) {
        Check.invariant(parameters.size() == 1, "exactly one parameter expected");
        return parameters.values().iterator().next().get();
    }
}
