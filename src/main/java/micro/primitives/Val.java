package micro.primitives;

import micro.Check;
import micro.Value;

import java.util.List;

public class Val implements Primitive {
    @Override
    public Object execute(List<Value> parameters) {
        Check.invariant(parameters.size() == 1, "exactly one parameter expected");
        return parameters.get(0).get();
    }
}
