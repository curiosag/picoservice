package micro.primitives;

import micro.Check;
import micro.Value;

import java.util.Map;

public class Pong implements Primitive {

    @Override
    public Object execute(Map<String, Value> parameters) {
        Check.invariant(parameters.size() == 1);
        return parameters.values().iterator().next();
    }
}
