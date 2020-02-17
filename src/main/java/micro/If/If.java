package micro.If;

import micro.*;

public class If extends F {

    public If() {
        super(Names.condition, Names.onTrue, Names.onFalse);
    }

    public void addPropagation(PropagationType propagationType, String nameExpected, String namePropagated, F to) {
        addPropagation(new IfPropagation(propagationType, nameExpected, namePropagated, to));
    }

    public void addPropagation(PropagationType propagationType, String name, F to) {
        addPropagation(propagationType, name, name, to);
    }

    @Override
    public If setLabel(String label) {
        super.setLabel(label);
        return this;
    }

    @Override
    public Ex newExecution(Env env, Ex returnTo) {
        return new ExIf(env, this, returnTo);
    }

}
