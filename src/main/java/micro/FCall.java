package micro;

public class FCall extends F {

    protected final F called;

    public FCall(Node node, F called, String... formalParams) {
        super(node, nop, formalParams);
        this.called = called;
    }

    @Override
    public _Ex createExecution(Node node, _Ex returnTo) {
        return new ExFCall(node, this, returnTo);
    }

}

