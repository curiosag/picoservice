package miso.ingredients;

public class Sub extends BinOp<Integer, Integer> {

    public Sub() {
        super((n1, n2) -> n1 - n2, o -> (Integer) o);
    }

    public static Sub sub(){
        return new Sub();
    }
}
