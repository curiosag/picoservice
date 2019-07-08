package miso.ingredients;

public class Eq extends BinOp<Integer, Boolean> {
    public Eq() {
        super(Integer::equals, o -> (Integer) o);
    }

    public static Eq eq() {
        return new Eq();
    }
}