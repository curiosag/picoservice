package micro;

public class ExTop extends ExF {

    public ExTop(Env env) {
        super(env);
    }

    @Override
    public void process(Value v) {
        if(v.getName().equals(Names.exception))
        {
            throw new RuntimeException((Exception) v.get());
        }
    }

    @Override
    public String toString() {
        return "TOP";
    }
}
