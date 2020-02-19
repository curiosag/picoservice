package micro.If;

import micro.*;


public class If extends F {

    public If() {
        super(Names.condition, Names.onTrue, Names.onFalse);
    }

    public void addPropagation(PropagationType propagationType, String nameExpected, String namePropagated, _F to) {
        addPropagation(new IfPropagation(propagationType, nameExpected, namePropagated, to));
    }

    public void addPropagation(PropagationType propagationType, String name, _F to) {
        addPropagation(propagationType, name, name, to);
    }

    @Override
    public void addPropagation(String name, _F to) {
        throw new IllegalStateException();
    }

    @Override
    public void addPropagation(String nameExpected, String namePropagated, _F to) {
        throw new IllegalStateException();
    }


    @Override
    public If label(String label) {
        super.label(label);
        return this;
    }

    @Override
    public Ex createExecution(Env env, Ex returnTo) {
        return new ExIf(env, this, returnTo);
    }

    public static If iff(){
        return new If();
    }
}
