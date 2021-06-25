package micro;

import micro.primitives.Primitive;

public class FCall extends F {

    private final F called;

    public FCall(F called) {
        super(Primitive.nop, called.formalParameters);
        this.called = called;
    }

    @Override
    public Ex createExecution(long exId, _Ex returnTo, Env env) {
        return new ExFCall(env, exId, this, returnTo);
    }

    public F getCalled() {
        return called;
    }

    @Override
    public String getLabel() {
        return "call:" + super.getLabel();
    }

    public static FCall fCall(Env env, F called) {
        FCall result = new FCall(called);
        env.register(result);
        return result;
    }
}

