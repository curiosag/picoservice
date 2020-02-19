package micro.atoms;

import micro.Value;

import java.util.Map;

public class Const implements Atom {

    private final Object value;

    public Const(Object value){
        this.value = value;
    }

    @Override
    public Object execute(Map<String, Value> values) {
        return value;
    }

}
