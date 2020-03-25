package micro;

import micro.atoms.Atom;

public class FCall extends F {

    protected final F called;

    public FCall(Node node, F called, String... formalParams) {
        super(node, Atom.nop, formalParams);
        this.called = called;
    }

    @Override
    public _Ex createExecution(Node node, _Ex returnTo) {
        return new ExFCall(node, this, returnTo);
    }

}

