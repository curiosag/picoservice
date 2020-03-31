package micro.primitives;

import micro.Check;
import micro.Value;

import java.util.Map;

public class As {

    public static Integer Integer(Map<String, Value> parameters, String name) {
        Value value = null;
        try {
            value = getParam(parameters, name);
            if (!(value.get() instanceof Integer)) {
                throw new IllegalArgumentException(String.format("param %s expected as Integer but is %s", name,
                        value.get().getClass().getSimpleName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (Integer) value.get();
    }

    public static Comparable Comparable(Map<String, Value> parameters, String name) {
        Value value = getParam(parameters, name);
        if (!(value.get() instanceof Comparable)) {
            throw new IllegalArgumentException(String.format("param %s expected as Comparable but is %s", name,
                    value.get().getClass().getSimpleName()));
        }
        return (Comparable) value.get();
    }

    private static Value getParam(Map<String, Value> parameters, String name) {
        Check.notNull(parameters);
        Check.notNull(name);

        Value value = parameters.get(name);
        if (value == null) {
            throw new IllegalArgumentException("param missing: " + name);
        }
        return value;
    }

}
