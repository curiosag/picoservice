package micro.primitives;

public class Add extends BinOp {

    public Add() {
        super((i,j) -> i + j);
    }

    public static Add add(){
        return new Add();
    }

}
