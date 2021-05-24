package micro;

import micro.primitives.Primitive;

import java.util.HashMap;
import java.util.Map;

public class FCall extends F {

    private final F called;

    Map<String, String> paramNameMapping = new HashMap<>();

    public FCall addParamNameMapping(String from, String to){
        paramNameMapping.put(from, to);
        return this;
    }

    public String mapParamName(String name){
        String result = paramNameMapping.get(name);
        if(result == null)
            return name;
        return result;
    }

    public FCall(Env env, F called) {
        super(env, Primitive.nop, called.formalParameters);
        this.called = called;
    }

    @Override
    public Ex createExecution(long exId, _Ex returnTo) {
        return new ExFCall(this.env, exId, this, returnTo);
    }

    public F getCalled() {
        return called;
    }

    @Override
    public String getLabel() {
        return "fcall:" + super.getLabel() + '/' + called.getLabel();
    }
}

