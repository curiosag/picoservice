package micro;

public class ExTop extends ExF {

    public ExTop(Env env) {
        super(env);
    }

    @Override
    public void process(Value v) {
    }

    @Override
    public String toString() {
        return "TOP";
    }
}
