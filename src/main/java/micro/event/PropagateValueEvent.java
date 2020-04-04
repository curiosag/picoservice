package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.*;

public final class PropagateValueEvent extends ExEvent {
    public Value value;
    public ExOnDemand to;
    private long exIdTo;

    public PropagateValueEvent() {
    }

    public PropagateValueEvent(Ex from, ExOnDemand to, Value value) {
        super(from);
        this.value = value;
        this.to = to;
        this.exIdTo = to.getId();
    }

    @Override
    public void hydrate(Hydrator h) {
        Check.fail("shouldn't ever be persisted");
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeVarLong(exIdTo, true);
        value.write(kryo, output);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        exIdTo = input.readVarLong(true);
        value = new Value();
        value.read(kryo, input);
    }

    @Override
    public String toString() {
        return "{\"PropagateValueEvent\":{" +
                "\"value\":" + value +
                ", \"to\":" + to +
                "}}";
    }
}