package miso.ingredients;

public class Gt extends BinOp<Integer, Boolean> {
    public Gt() {
        super((n1, n2) -> n1 > n2, o -> (Integer) o);
    }

    public static Gt gt() {
        return new Gt();
    }
}
