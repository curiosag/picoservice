package micro;

import micro.event.DependendExCreatedEvent;
import micro.event.ExEvent;
import micro.event.ValueEnqueuedEvent;

public class ExFCall extends Ex {

    private FCall callTemplate; //TODO zu kompliziert, wieso nicht gleich "called" hier?
    private _Ex beingCalled;
    private boolean dependendExCreatedEventRaised;

    ExFCall(Env env, long exId, FCall callTemplate, _Ex returnTo) {
        super(env, exId, callTemplate, returnTo);
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
            return env.createDependentExecutionEvent(callTemplate.getCalled(), this, this);
        }
        return none;
    }

    @Override
    public void processValueDownstream(Value v) {
        if (isRecovery) {
            return;
        }
        Check.preCondition(isDownstream(v.getName()));
        propagate(v);
        if (callTemplate.formalParameters.contains(v.getName())) {
            deliver(v.withSender(this), getBeingCalled());
        }
    }

    private _Ex getBeingCalled() {
        Check.preCondition(beingCalled != null);
        return beingCalled;
    }

}
