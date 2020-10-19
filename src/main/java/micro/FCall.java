package micro;

import micro.primitives.Primitive;

public class FCall extends F {

    protected final F called;

    public FCall(Node node, F called, String... formalParams) {
        super(node, Primitive.nop, formalParams);
        this.called = called;
    }

    @Override
    public _Ex createExecution(_Ex returnTo) {
        return new ExFCall(this.node, this, returnTo);
    }

}

