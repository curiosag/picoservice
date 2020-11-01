package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

import static micro.Check.notNull;

public class ExFCallByFunctionalValue extends Ex implements Hydratable {
    long idf;

    FCallByFunctionalValue f;
    _F baseFunction;
    _Ex beingCalled;
    private final List<Value> pendingValues = new ArrayList<>();

    ExFCallByFunctionalValue(Node node, FCallByFunctionalValue f, _Ex returnTo) {
        super(node, f, returnTo);
        this.f = f;
    }

    @Override
    protected void alterStateFor(Value v) {
        super.alterStateFor(v);
        if (isFunctionInputValue(v)) {
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
        }
    }

    @Override
    protected void performValueReceived(Value v) {
        if (v.getName().equals(f.getFunctionalValueParam())) {
            pendingValues.forEach(pv -> getFunctionBeingCalled().receive(pv.withSender(this)));
        } else {
            Check.isLegitInputValue(v);
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
            beingCalled = node.getExecution(notNull(baseFunction), this);
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
