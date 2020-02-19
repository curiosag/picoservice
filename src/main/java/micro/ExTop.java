package micro;

public class ExTop extends ExF {

    public static ExTop TOP = new ExTop();

    @Override
    public ExTop accept(Value v) {
        return this;
    }

    @Override
    public String toString() {
        return "TOP";
    }
}
