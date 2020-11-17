package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

public class ExFCallByFunctionalValue extends Ex implements Hydratable {
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
    protected int getNumberCustomIdsNeeded() {
        return 1; // for one Ex of f
    }

    @Override
    protected void processDownstreamValue(Value v) {
        Check.preCondition(isLegitDownstreamValue(v));

        if (v.getName().equals(f.getFunctionalValueParam())) {
            Check.invariant(v.get() instanceof PartiallyAppliedFunction, "that wasn't expected: " + v.toString());
            PartiallyAppliedFunction p = ((PartiallyAppliedFunction) v.get());
            baseFunction = p.baseFunction;
            pendingValues.addAll(p.partialValues);
        } else {
            if (baseFunction == null) {
                pendingValues.add(v);
            }
        }

        if (v.getName().equals(f.getFunctionalValueParam())) {
            pendingValues.forEach(pv -> getFunctionBeingCalled().receive(pv.withSender(this)));
        } else {
            if (baseFunction != null) {
                getFunctionBeingCalled().receive(v.withSender(this));
            }
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
        if (beingCalled == null) {
            beingCalled = baseFunction.createExecution(getNextExId(), this);
        }
        return beingCalled;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeVarLong(f.getId(), true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        idf = input.readVarLong(true);
    }

    @Override
    public void hydrate(Hydrator h) {
        _F i = h.getFForId(idf);
        Check.invariant(i instanceof FCallByFunctionalValue, "..?");
        //noinspection ConstantConditions
        f = (FCallByFunctionalValue) i;
    }
}
