package micro.primitives;

public class Sub extends BinOp {

    public static Sub sub = new Sub();

    private Sub() {
        super((i,j) -> i - j);
    }

}
