package micro.primitives;

import micro.Check;
import micro.Value;

import java.util.Map;

public class Not implements Primitive {

    public static Not not = new Not();

    @Override
    public Object execute(Map<String, Value> parameters) {
        Check.invariant(parameters.size() == 1);
        if (parameters.get(0).get() instanceof Boolean bool)
            return !bool;
        else
            throw new IllegalStateException();
    }

    public static Not not() {
        return not;
    }
}
