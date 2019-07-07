package miso.ingredients;

public class Or extends BinOp<Boolean, Boolean> {
    public Or() {
        super((n1, n2) -> n1 | n2, o -> (Boolean) o);
    }

    public Or or() {
        return new Or();
    }
}
