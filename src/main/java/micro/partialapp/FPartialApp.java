package micro.partialapp;

import micro.*;

import java.util.HashMap;
import java.util.Map;

public class FPartialApp implements _F {

    public final F inner;

    public Map<String, String> partials = new HashMap<>();

    private FPartialApp(F inner) {
        this.inner = inner;
    }

    public ResolvedBy withParam(String name) {
        return new ResolvedBy(this, name);
    }

    void addPartial(String name, String ref){
        partials.put(name, ref);
    }

    @Override
    public void addPropagation(String name, _F to) {
        inner.addPropagation(name, to);
    }

    @Override
    public void addPropagation(String nameExpected, String namePropagated, _F to) {
        inner.addPropagation(nameExpected, namePropagated, to);
    }

    @Override
    public Ex createExecution(Env env, Ex returnTo) {
        Ex result = inner.createExecution(env, returnTo);
        partials.forEach((name, value) -> result.process(new Value(name, value, result)));
        return result;
    }

    public static FPartialApp partial(F inner) {
        return new FPartialApp(inner);
    }
}
