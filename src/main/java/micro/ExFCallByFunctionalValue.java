package micro;

import micro.event.ValueReceivedEvent;

import java.util.ArrayList;
import java.util.List;

import static micro.Check.notNull;

public class ExFCallByFunctionalValue extends Ex {

    private FCallByFunctionalValue f;
    private F baseFunction;
    private _Ex beingCalled;
    private List<Value> pendingValues = new ArrayList<>();

    ExFCallByFunctionalValue(Node node, FCallByFunctionalValue f, _Ex returnTo) {
        super(node, f, returnTo);
        this.f = f;
    }

    @Override
    public String getLabel() {
        return f.getLabel();
    }

    String getNameForReturnValue() {
        return f.returnAs;
    }

    @Override
    public void perfromValueReceived(Value v) {
        if (v.getName().equals(f.getFunctionalValueParam())) {
            Check.invariant(v.get() instanceof PartiallyAppliedFunction, "that wasn't expected: " + v.toString());
            PartiallyAppliedFunction p = ((PartiallyAppliedFunction) v.get());
            baseFunction = p.baseFunction;
            pendingValues.addAll(p.partialValues);
            pendingValues.forEach(pv -> raise(new ValueReceivedEvent(node.getNextObjectId(), getBeingCalled(), pv)));
        } else {
            Check.isFunctionInputValue(v);
            if (baseFunction == null) {
                pendingValues.add(v);
            } else {
                getBeingCalled().receive(v.withSender(this));
            }
        }
    }

    private _Ex getBeingCalled() {
        if (beingCalled == null) {
            beingCalled = node.getExecution(notNull(baseFunction), this);
        }
        return beingCalled;
    }

}
