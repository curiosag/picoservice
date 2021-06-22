package micro.primitives;

public class Minus extends BinOp {

    public static Minus minus = new Minus();

    public Minus() {
        super((i, j) -> {
            if (i instanceof Integer x && j instanceof Integer y)
                return x - y;
            if (i instanceof Long x && j instanceof Long y)
                return x - y;
            if (i instanceof Character x && j instanceof Character y)
                return x - y;
            if (i instanceof Byte x && j instanceof Byte y)
                return x - y;
            if (i instanceof Float x && j instanceof Float y)
                return x - y;
            if (i instanceof Double x && j instanceof Double y)
                return x - y;
            throw new IllegalStateException("Type not covered for BinOp: " + i.getClass().getSimpleName());
        });
    }

    public static Minus minus() {
        return minus;
    }
}
