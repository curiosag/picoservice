package micro;

import micro.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExFCallByFunctionalValue extends Ex {

    FCallByFunctionalValue f;
    _F baseFunction;
    _Ex beingCalled;

    private final List<Value> pendingValues = new ArrayList<>();
    private boolean dependentExCreated;

    ExFCallByFunctionalValue(Node node, long id, FCallByFunctionalValue f, _Ex returnTo) {
        super(node, id, f, returnTo);
        this.f = f;
    }

    @Override
    protected void handleKarma(KarmaEvent k) {
        Check.condition(k instanceof KarmaEventCanPropagatePendingValues);
        Check.preCondition(dependentExCreated);

        pendingValues.forEach(pv -> deliver(pv.withSender(this), beingCalled));
        pendingValues.clear();
    }

    @Override
    protected boolean customEventHandled(ExEvent e) {
        if (e instanceof DependendExCreatedEvent) {
            dependentExCreated = true; // needs to be set in case of recovery
            beingCalled = e.getEx();
            Check.postCondition((isRecovery && baseFunction == null) || beingCalled.getTemplate().equals(baseFunction));
            return true;
        }

        return false;
    }

    @Override
    protected Optional<KarmaEvent> getAfterlife(ValueEnqueuedEvent e) {
        if (isFunctionalValueParam(e)) {
            return Optional.of(new KarmaEventCanPropagatePendingValues(this));
        }
        return Optional.empty();
    }

    private boolean isFunctionalValueParam(ValueEvent e) {
        return e.value.getName().equals(f.getFunctionalValueParam());
    }

    @Override
    protected ExEvent getEventTriggeredAfterCurrent(ValueEnqueuedEvent e) {
        if (!dependentExCreated && isFunctionalValueParam(e)) {
            dependentExCreated = true;
            return node.createDependentExecutionEvent(baseFunction, this, this);
        }
        return none;
    }

    private void acceptFunctionalValueTemplate(Object value) {
        Check.invariant(value instanceof PartiallyAppliedFunction, "that wasn't expected: " + value);
        PartiallyAppliedFunction f = ((PartiallyAppliedFunction) value);
        baseFunction = f.baseFunction;
        pendingValues.addAll(f.partialValues);
    }

    @Override
    protected void processValueDownstream(Value v) {
        Check.preCondition(isLegitDownstreamValue(v));

        if (v.getName().equals(f.getFunctionalValueParam())) {
            acceptFunctionalValueTemplate(v.get());
        } else if (beingCalled == null) {
            pendingValues.add(v);
        } else {
            deliver(v.withSender(this), getFunctionBeingCalled());
        }
    }

    @Override
    void clear() {
        beingCalled = null;
        baseFunction = null;
        f = null;
        pendingValues.clear();
        super.clear();
    }

    @Override
    public String getLabel() {
        return f.getLabel();
    }

    String getNameForReturnValue() {
        return f.returnAs;
    }

    private _Ex getFunctionBeingCalled() {
        Check.invariant(beingCalled != null);
        return beingCalled;
    }

}
