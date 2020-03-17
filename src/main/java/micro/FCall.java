package micro;

public class FCall extends F {

    protected final F called;

    public FCall(Env env, F called, String... formalParams) {
        super(env, nop, formalParams);
        this.called = called;
    }

    @Override
    public _Ex createExecution(Env env, _Ex returnTo) {
        return new ExFCall(env, this, returnTo);
    }

}

