package micro;

import micro.event.DependendExCreatedEvent;
import micro.event.ExEvent;
import micro.event.ValueEnqueuedEvent;

public class ExFCall extends Ex {

    private FCall callTemplate; //TODO zu kompliziert, wieso nicht gleich "called" hier?
    private _Ex beingCalled;
    private boolean dependendExCreatedEventRaised;

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
    protected boolean customEventHandled(ExEvent e) {
        if (e instanceof DependendExCreatedEvent) {
            acceptExBeingCalled((DependendExCreatedEvent)e);
            return true;
        }
        return false;
    }

    private void acceptExBeingCalled(DependendExCreatedEvent e) {
        Check.invariant(e.getEx().getTemplate().equals(callTemplate.getCalled()));
        beingCalled = e.getEx();
        dependendExCreatedEventRaised = true; // for recovery case
    }

    @Override
    protected ExEvent getEventTriggeredBeforeCurrent(ValueEnqueuedEvent value) {
        if (! dependendExCreatedEventRaised) {
            dependendExCreatedEventRaised = true;
            return node.createDependentExecutionEvent(callTemplate.getCalled(), this, this);
        }
        return none;
    }

    @Override
    public void processValueDownstream(Value v) {
        if (isRecovery) {
            return;
        }
        Check.preCondition(isLegitDownstreamValue(v));
        propagate(v);
        if (callTemplate.formalParameters.contains(v.getName())) {
            deliver(v.withSender(this), getBeingCalled());
        }
    }

    private _Ex getBeingCalled() {
        Check.preCondition(beingCalled != null);
        return beingCalled;
    }

    @Override
    void clear() {
        beingCalled = null;
        callTemplate = null;
        super.clear();
    }

}
