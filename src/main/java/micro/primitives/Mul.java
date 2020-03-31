package micro.primitives;

public class Mul extends BinOp {

    public Mul() {
        super((i,j) -> i * j);
    }

    public static Mul mul(){
        return new Mul();
    }

}
