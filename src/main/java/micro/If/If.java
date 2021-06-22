package micro.If;

import micro.*;
import micro.primitives.Primitive;


public class If extends F {

    public If() {
        super(Primitive.nop, Names.condition, Names.onTrue, Names.onFalse);
    }

    public void addPropagation(PropagationType propagationType, String name, _F to) {
        addPropagation(propagationType, name, name, to);
    }

    @Override
    public void addPropagation(String name, _F to) {
        throw new IllegalStateException();
    }

    @Override
    public If label(String label) {
        super.label(label);
        return this;
    }

    @Override
    public Ex createExecution(long id, _Ex returnTo, Env env) {
        return new ExIf(env, id, this, returnTo);
    }

    public static If iff(Env env){
        If result = new If();
        env.register(result);
        return result;
    }
}
