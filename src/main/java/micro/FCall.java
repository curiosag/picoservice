package micro;

import micro.primitives.Primitive;

import java.util.HashMap;
import java.util.Map;

public class FCall extends F {

    private final F called;

    Map<String, String> paramNameMapping = new HashMap<>();

    private FCall(F called) {
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
        return "fcall:" + super.getLabel() + '\n' + called.getLabel();
    }

    public static FCall fCall(Env env, F called){
        FCall result = new FCall(called);
        env.register(result);
        return result;
    }
}

