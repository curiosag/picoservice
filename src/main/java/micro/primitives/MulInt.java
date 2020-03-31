package micro.primitives;

import micro.Names;
import micro.Value;

import java.util.Map;

public class MulInt implements Primitive {

    @Override
    public Object execute(Map<String, Value> params) {
        return  As.Integer(params, Names.left) * As.Integer(params, Names.right);
    }

}
