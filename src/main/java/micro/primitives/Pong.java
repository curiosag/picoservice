package micro.primitives;

import micro.Check;
import micro.Value;

import java.util.Map;

public class Pong implements Primitive {

    public static Pong pong = new Pong();

    @Override
    public Object execute(Map<String, Value> parameters) {
        Check.invariant(parameters.size() == 1);
        return parameters.values().iterator().next();
    }
}
