package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PartiallyAppliedFunction implements Hydratable, KryoSerializable {

    private long idBaseFunction;
    _F baseFunction;
    List<Value> partialValues = new ArrayList<>();

    PartiallyAppliedFunction(F baseFunction, Collection<Value> partialValues) {
        this.baseFunction = baseFunction;
        this.partialValues.addAll(partialValues);
    }

    public PartiallyAppliedFunction() {
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeVarLong(baseFunction.getId(), true);
        output.writeVarInt(partialValues.size(), true);
        partialValues.forEach(p -> p.write(kryo, output));
    }

    @Override
    public void read(Kryo kryo, Input input) {
        idBaseFunction = input.readVarLong(true);
        int size = input.readVarInt(true);
        for (int i = 0; i < size; i++) {
            Value v = new Value();
            v.read(kryo, input);
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
    public String toString() {
        return "{\"PartiallyAppliedFunction\":{" +
                "\"baseFunction\":" + baseFunction.getId() +
                ", \"partialValues\":" + partialValues +
                "}}";
    }

    @Override
    public long getId() {
        throw new IllegalStateException();
    }

    @Override
    public void setId(long value) {
        throw new IllegalStateException();
    }
}
