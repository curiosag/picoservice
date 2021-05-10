package micro.primitives;

import micro.Value;

import java.util.Map;

public class Constant implements Primitive{

    private final Object constValue;

    public Constant(Object constValue) {
        this.constValue = constValue;
    }

    @Override
    public Object execute(Map<String, Value> parameters) {
        return constValue;
    }
}
