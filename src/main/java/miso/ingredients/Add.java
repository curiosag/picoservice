package miso.ingredients;

public class Add extends BinOp<Integer, Integer> {

    public Add() {
        super((n1, n2) -> n1 + n2, o -> (Integer) o);
    }

    public static Add add(){
        return new Add();
    }
}
