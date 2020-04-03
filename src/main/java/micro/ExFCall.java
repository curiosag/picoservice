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
        Check.isFunctionInputValue(v);
        getBeingCalled().receive(v.withSender(this));
    }

    private _Ex getBeingCalled() {
        if (beingCalled == null) {
            beingCalled = node.getExecution(this, callTemplate.called);
        }
        return beingCalled;
    }

    @Override
    void clear() {
        beingCalled = null;
        callTemplate = null;
        super.clear();
    }

    @Override
    public String toString() {
        return "{\"ExFCall\":{" +
                "\"id\":" + getId() +
                ", \"template\":" + template.getId() +
                ", \"returnTo\":" + returnTo.getId() +
                ", \"paramsReceived\":" + paramsReceived +
                ", \"callTemplate\":" + callTemplate.getId() +
                ", \"beingCalled\":" + (beingCalled == null ? null : beingCalled.getId()) +
                "}}";
    }
}
