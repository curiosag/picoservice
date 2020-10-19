package micro;

public class ExFCall extends Ex {

    private FCall callTemplate;
    private _Ex beingCalled;

    ExFCall(Node node, FCall callTemplate, _Ex returnTo) {
        super(node, callTemplate, returnTo);
        this.callTemplate = callTemplate;
    }

    @Override
    public String getLabel() {
        return callTemplate.getLabel();
    }

    String getNameForReturnValue() {
        return callTemplate.returnAs;
    }

    @Override
    public void performValueReceived(Value v) {
        Check.isLegitInputValue(v);
        propagate(v);
        if (callTemplate.formalParameters.contains(v.getName())) {
            getBeingCalled().receive(v.withSender(this));
        }
    }

    private _Ex getBeingCalled() {
        if (beingCalled == null) {
            beingCalled = node.getExecution(callTemplate.getCalled(), this);
        }
        return beingCalled;
    }

    @Override
    void clear() {
        beingCalled = null;
        callTemplate = null;
        super.clear();
    }

}
