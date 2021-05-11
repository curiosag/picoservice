package micro;

import micro.event.CustomEventHandling;
import micro.event.ExEvent;
import micro.event.ExecutionCreatedEvent;

import java.util.Optional;

public class ExFCall extends Ex {

    private FCall callTemplate; //TODO zu kompliziert, wieso nicht gleich "called" hier?
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
    protected CustomEventHandling customEventHandling(ExEvent e) {
        if (beingCalled == null && e instanceof ExecutionCreatedEvent) {
            acceptExBeingCalled(e);
            return CustomEventHandling.consuming;
        }
        return CustomEventHandling.none;
    }

    private void acceptExBeingCalled(ExEvent e) {
        Check.invariant(e.getEx().getTemplate().equals(callTemplate.getCalled()));
        beingCalled = e.getEx();
    }

    @Override
    protected Optional<ExEvent> raiseCustomEvent(Value value) {
        if (beingCalled != null) {
            return Optional.empty();
        }
        return Optional.of(new ExecutionCreatedEvent((Ex) node.createExecution(callTemplate.getCalled(), this))); //TODO (Ex)?
    }

    @Override
    public void processValueDownstream(Value v) {
        Check.preCondition(isLegitDownstreamValue(v));
        propagate(v);
        if (callTemplate.formalParameters.contains(v.getName())) {
            getBeingCalled().receive(v.withSender(this));
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
