package micro.primitives;

public class And extends BinOp {

    public static And and = new And();

    public And() {
        super((i, j) -> {
            if (i instanceof Boolean x && j instanceof Boolean y)
                return x & y;
            else
                throw new IllegalStateException("Type not covered for And: " + i.getClass().getSimpleName());
        });
    }

    public static And and() {
        return and;
    }

}
