package micro.primitives;

public class Or extends BinOp {

    public static Or or = new Or();

    public Or() {
        super((i, j) -> {
            if (i instanceof Boolean x && j instanceof Boolean y)
                return x | y;
            else
                throw new IllegalStateException("Type not covered for And: " + i.getClass().getSimpleName());
        });
    }

    public static Or or() {
        return or;
    }

}
