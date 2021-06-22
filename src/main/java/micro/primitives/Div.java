package micro.primitives;

public class Div extends BinOp {

    public static Div div = new Div();

    public Div() {
        super((i, j) -> {
            if (i instanceof Integer x && j instanceof Integer y)
                return x / y;
            if (i instanceof Long x && j instanceof Long y)
                return x / y;
            if (i instanceof Character x && j instanceof Character y)
                return x / y;
            if (i instanceof Byte x && j instanceof Byte y)
                return x / y;
            if (i instanceof Float x && j instanceof Float y)
                return x / y;
            if (i instanceof Double x && j instanceof Double y)
                return x / y;
            throw new IllegalStateException("Type not covered for BinOp: " + i.getClass().getSimpleName());
        });
    }

    public static Div div() {
        return div;
    }


}
