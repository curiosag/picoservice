package micro;

public class ExTop extends ExF {

    public static ExTop TOP = new ExTop();

    @Override
    public void accept(Value v) {
    }

    @Override
    public String toString() {
        return "TOP";
    }
}
