package micro;

import micro.event.serialization.Incoming;
import micro.event.serialization.Outgoing;
import micro.event.serialization.Serioulizable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PartiallyAppliedFunction implements Hydratable, Serializable, Serioulizable {

    private long idBaseFunction;
    _F baseFunction;
    List<Value> partialValues = new ArrayList<>();

    PartiallyAppliedFunction(F baseFunction, Collection<Value> partialValues) {
        Guards.notNull(baseFunction);
        Guards.notNull(partialValues);
        this.baseFunction = baseFunction;
        this.idBaseFunction = baseFunction.getId();
        this.partialValues.addAll(partialValues);
    }

    public PartiallyAppliedFunction() {
    }

    @Override
    public void write(Outgoing output) {
        output.writeVarLong(baseFunction.getId(), true);
        output.writeVarInt(partialValues.size(), true);
        partialValues.forEach(p -> p.write(output));
    }

    @Override
    public void read(Incoming input) {
        idBaseFunction = input.readVarLong(true);
        int size = input.readVarInt(true);
        for (int i = 0; i < size; i++) {
            Value v = new Value();
            v.read(input);
            partialValues.add(v);
        }
    }

    @Override
    public void hydrate(Hydrator h) {
        if (baseFunction == null) {
            baseFunction = h.getFForId(idBaseFunction);
            partialValues.forEach(v -> v.hydrate(h));
        }
    }

    @Override
    public void dehydrate() {
        baseFunction = null;
    }
}
