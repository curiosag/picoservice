package micro;

import micro.event.CustomEventHandling;
import micro.event.ExEvent;
import micro.event.ExCreatedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExFCallByFunctionalValue extends Ex {

    long idf;

    FCallByFunctionalValue f;
    _F baseFunction;
    _Ex beingCalled;

    private final List<Value> pendingValues = new ArrayList<>();

    ExFCallByFunctionalValue(Node node, long id, FCallByFunctionalValue f, _Ex returnTo) {
        super(node, id, f, returnTo);
        this.f = f;
    }

    @Override
    protected CustomEventHandling customEventHandling(ExEvent e) {
        if (e instanceof ExCreatedEvent) {
            acceptFunctionalValueEx(e.getEx());
            return CustomEventHandling.consuming;
        }
        return CustomEventHandling.none;
    }

    private void acceptFunctionalValueEx(_Ex e) {
        Check.invariant(e.getTemplate().equals(baseFunction));
        beingCalled = e;
        pendingValues.forEach(pv -> deliver(pv.withSender(this), beingCalled));
    }

    @Override
    protected Optional<ExEvent> raiseCustomEvent(Value value) {
        if (baseFunction == null && value.getName().equals(f.getFunctionalValueParam())) {
            acceptFunctionalValueTemplate(value);
            return Optional.of(new ExCreatedEvent((Ex) node.createExecution(baseFunction, this))); //TODO (Ex)?
        }

        return Optional.empty();
    }

    private void acceptFunctionalValueTemplate(Value value) {
        Check.invariant(value.get() instanceof PartiallyAppliedFunction, "that wasn't expected: " + value);
        PartiallyAppliedFunction f = ((PartiallyAppliedFunction) value.get());
        baseFunction = f.baseFunction;
        pendingValues.addAll(f.partialValues);
    }

    @Override
    protected void processValueDownstream(Value v) {
        Check.preCondition(isLegitDownstreamValue(v));

        if (v.getName().equals(f.getFunctionalValueParam())) {
            return; // handled in raiseCustomEvent
        }

        if (beingCalled != null) {
            deliver(v.withSender(this), getFunctionBeingCalled());
        } else {
            pendingValues.add(v);
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
