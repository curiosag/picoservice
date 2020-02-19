package micro.atoms;

import micro.Names;
import micro.Value;

import java.util.Map;

public class SubInt implements Atom {

    @Override
    public Object execute(Map<String, Value> params) {
        return As.Integer(params, Names.left) - As.Integer(params, Names.right);
    }

    public static SubInt subInt() {
        return new SubInt();
    }

}
