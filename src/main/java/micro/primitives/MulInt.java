package micro.primitives;

import micro.Names;
import micro.Value;

import java.util.List;

public class MulInt implements Primitive {

    @Override
    public Object execute(List<Value> params) {
        return  As.Integer(params, Names.left) * As.Integer(params, Names.right);
    }

}
