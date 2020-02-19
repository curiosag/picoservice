package micro.atoms;

import micro.Names;
import micro.Value;

import java.util.Map;

public class AddInt implements Atom {

    private static int instances;

    private int instance;

    public AddInt() {
        instance = instances++;
    }

    @Override
    public Object execute(Map<String, Value> params) {
        return  As.Integer(params, Names.left) + As.Integer(params, Names.right);
    }

    public static AddInt addInt (){
        return new AddInt();
    }

}
