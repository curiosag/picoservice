package micro.primitives;

import micro.Names;
import micro.Value;

import java.util.List;

public class Gt implements Primitive {

    @Override
    public Object execute(List<Value> params) {
        return As.Comparable(params, Names.left).compareTo(As.Comparable(params, Names.right)) > 0;
    }

    public static Gt gt(){
        return new Gt();
    }
}
