package micro.primitives;

import micro.Value;

import java.util.List;

public class Const implements Primitive {

    private final Object value;

    public Const(Object value){
        this.value = value;
    }

    @Override
    public Object execute(List<Value> values) {
        return value;
    }

}
