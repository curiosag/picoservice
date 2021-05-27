package micro.primitives;

public class Mul extends BinOp {

    public static Mul mul = new Mul();

    public Mul() {
        super((i,j) -> i * j);
    }

    public static Mul mul(){
        return mul;
    }

}
