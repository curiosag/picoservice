package miso.ingredients;

public class Plus extends BinOp<Integer, Integer> {

    public Plus() {
        super((n1, n2) -> n1 + n2, o -> (Integer) o);
    }

    public static Plus plus(){
        return new Plus();
    }
}
