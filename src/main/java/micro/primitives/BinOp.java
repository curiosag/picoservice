package micro.primitives;

import micro.Names;
import micro.Value;

import java.util.List;
import java.util.function.BiFunction;

public abstract class BinOp implements Primitive {

    private final BiFunction<Integer, Integer, Integer> binOp;

    public BinOp(BiFunction<Integer, Integer, Integer> binOp) {
        this.binOp = binOp;
    }

    @Override
    public Object execute(List<Value> params) {
        return binOp.apply(As.Integer(params, Names.left), As.Integer(params, Names.right));
    }

}
