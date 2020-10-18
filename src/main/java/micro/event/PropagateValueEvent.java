package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.*;

public class PropagateValueEvent extends ExEvent implements Hydratable {
    public Value value;
    public long idToF;
    public _F toF;

    public PropagateValueEvent() {
    }

    public PropagateValueEvent(Ex from, _F toF, Value value) {
        super(from);
        this.value = value;
        this.toF = toF;
    }

    @Override
    public void hydrate(Hydrator h) {
        toF = h.getFForId(idToF);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeVarLong(toF.getId(), true);
        value.write(kryo, output);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        idToF = input.readVarLong(true);
        value = new Value();
        value.read(kryo, input);
    }

    @Override
    public String toString() {
        return "{\"PropagateValueEvent\":{" +
                "\"value\":" + value +
                ", \"toF\":" + toF +
                "}}";
    }
}