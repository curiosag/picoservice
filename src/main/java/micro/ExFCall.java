package micro;

public class ExFCall extends Ex {

    private FCall callTemplate;
    private _Ex beingCalled;

    ExFCall(Node node, FCall callTemplate, _Ex returnTo) {
        super(node, callTemplate, returnTo);
        this.callTemplate = callTemplate;
    }

    @Override
    public String getLabel(){
        return callTemplate.getLabel();
    }

    String getNameForReturnValue() {
        return callTemplate.returnAs;
    }

    @Override
    public void perfromValueReceived(Value v) {
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
