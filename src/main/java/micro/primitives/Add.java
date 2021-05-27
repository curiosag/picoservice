package micro.primitives;

public class Add extends BinOp {

    public static Add add = new Add();

    public Add() {
        super(Integer::sum);
    }

    public static Add add(){
        return add;
    }

}
