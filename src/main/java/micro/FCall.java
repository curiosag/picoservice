package micro;

import static micro.atoms.Nop.nop;

public class FCall extends F {

    protected final F called;

    public FCall(F called, String... formalParams) {
        super(nop, formalParams);
        this.called = called;
    }

    @Override
    public Ex createExecution(Env env, Ex returnTo) {
        return new ExFCall(env, this, returnTo);
    }

}

