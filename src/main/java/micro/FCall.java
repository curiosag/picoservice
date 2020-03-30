package micro;

import micro.atoms.Primitive;

public class FCall extends F {

    protected final F called;

    public FCall(Node node, F called, String... formalParams) {
        super(node, Primitive.nop, formalParams);
        this.called = called;
    }

    @Override
    public _Ex createExecution(Node node, _Ex returnTo) {
        return new ExFCall(node, this, returnTo);
    }

}

