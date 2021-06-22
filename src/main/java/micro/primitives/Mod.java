package micro.primitives;

public class Mod extends BinOp {

    public static Mod mod = new Mod();

    public Mod() {
        super((i, j) -> {
            if (i instanceof Integer x && j instanceof Integer y)
                return x % y;
            if (i instanceof Long x && j instanceof Long y)
                return x % y;
            if (i instanceof Character x && j instanceof Character y)
                return x % y;
            if (i instanceof Byte x && j instanceof Byte y)
                return x % y;
            if (i instanceof Float x && j instanceof Float y)
                return x % y;
            if (i instanceof Double x && j instanceof Double y)
                return x % y;
            throw new IllegalStateException("Type not covered for BinOp: " + i.getClass().getSimpleName());
        });
    }

    public static Mod mod() {
        return mod;
    }

}
