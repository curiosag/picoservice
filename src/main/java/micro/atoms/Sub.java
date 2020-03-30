package micro.atoms;

public class Sub extends BinOp {

    private Sub() {
        super((i,j) -> i - j);
    }

    public static Sub subInt() {
        return new Sub();
    }

}
