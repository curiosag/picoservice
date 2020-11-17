package micro;

import micro.primitives.Primitive;

public class FCall extends F {

    private final F called;

    public FCall(Node node, F called) {
        super(node, Primitive.nop, called.formalParameters);
        this.called = called;
    }

    @Override
    public Ex createExecution(long exId, _Ex returnTo) {
        return new ExFCall(this.node, exId,this, returnTo);
    }

    public F getCalled() {
        return called;
    }

    @Override
    public String getLabel() {
        return "call:" + called.getLabel();
    }
}

