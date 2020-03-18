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
    public void addPropagation(PropagationType type, String nameExpected, String namePropagated, _F to) {
        inner.addPropagation(type, nameExpected, namePropagated, to);
    }

    @Override
    public _Ex createExecution(Env env, _Ex returnTo) {
        _Ex result = inner.createExecution(env, returnTo);
        partials.forEach((name, value) -> result.accept(new Value(name, value, result)));
        return result;
    }

    public static FPartialApp partial(F inner) {
        return new FPartialApp(inner);
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void setId(long value) {

    }
}
