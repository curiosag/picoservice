package miso.ingredients;

public class Lt extends BinOp<Integer, Boolean> {
    private Lt() {
        super((n1, n2) -> n1 < n2, o -> (Integer) o);
    }

    public static Lt lt() {
        return new Lt();
    }
}
