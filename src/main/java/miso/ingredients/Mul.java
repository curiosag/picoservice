package miso.ingredients;

public class Mul extends BinOp<Integer, Integer> {

    public Mul() {
        super((n1, n2) -> n1 * n2, o -> (Integer) o);
    }

    public static Mul mul(){
        return new Mul();
    }
}
