package micro.If;

import micro.*;


public class If extends F {

    public If(Env env) {
        super(env, nop, Names.condition, Names.onTrue, Names.onFalse);
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
    public _Ex createExecution(Env env, _Ex returnTo) {
        return new ExIf(env, this, returnTo);
    }

    public static If iff(Env env){
        return new If(env);
    }
}
