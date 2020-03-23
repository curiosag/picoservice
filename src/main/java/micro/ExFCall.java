package micro;

public class ExFCall extends Ex {

    private FCall callTemplate;
    private _Ex beingCalled;

    ExFCall(Node node, FCall callTemplate, _Ex returnTo) {
        super(node, new F(node, F.nop).label(callTemplate.getLabel()), returnTo);
        this.callTemplate = callTemplate;
    }

    public ExFCall(Node node) {
        super(node);
    }

    String getNameForReturnValue() {
        return callTemplate.returnAs;
    }

    @Override
    public void perfromFunctionInputValueReceived(Value v) {
        Check.isFunctionInputValue(v);
        getBeingCalled().receive(v.withSender(this));
    }

    private _Ex getBeingCalled() {
        if (beingCalled == null) {
            beingCalled = node.getExecution(callTemplate.called, this);
        }
        return beingCalled;
    }

}
