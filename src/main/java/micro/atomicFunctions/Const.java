package micro.atomicFunctions;

import micro.Value;

import java.util.List;

public class Const implements Atom {

    private final Object value;

    public Const(Object value){
        this.value = value;
    }

    @Override
    public Object execute(List<Value> values) {
        return value;
    }

}
