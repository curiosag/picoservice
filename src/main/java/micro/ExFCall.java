package micro;

public class ExFCall extends Ex {

    private FCall callTemplate;
    private _Ex beingCalled;

    ExFCall(Node node, long exId, FCall callTemplate, _Ex returnTo) {
        super(node, exId, callTemplate, returnTo);
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
    protected int getNumberCustomIdsNeeded() {
        return 1; // for one Ex of callTemplate
    }

    @Override
    public void processDownstreamValue(Value v) {
        Check.preCondition(isLegitDownstreamValue(v));
        propagate(v);
        if (callTemplate.formalParameters.contains(v.getName())) {
            getBeingCalled().receive(v.withSender(this));
        }
    }

    private _Ex getBeingCalled() {
        if (beingCalled == null) {
            beingCalled = callTemplate.getCalled().createExecution(getNextExId(), this);
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
