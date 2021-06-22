package micro.primitives;

import micro.Names;
import micro.Value;

import java.util.Map;
import java.util.function.BinaryOperator;

public abstract class BinOp implements Primitive {

    private final BinaryOperator<Object> binOp;

    public BinOp(BinaryOperator<Object> binOp) {
        this.binOp = binOp;
    }

    @Override
    public Object execute(Map<String, Value> params) {
        return binOp.apply(params.get(Names.left).get(), params.get(Names.right).get());
    }

}
