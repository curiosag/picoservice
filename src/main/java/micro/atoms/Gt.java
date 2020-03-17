package micro.atoms;

import micro.Names;
import micro.Value;

import java.util.Map;

public class Gt implements Atom {

    @Override
    public Object execute(Map<String, Value> params) {
        return As.Comparable(params, Names.left).compareTo(As.Comparable(params, Names.right)) > 0;
    }

    public static Gt gt(){
        return new Gt();
    }
}