package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Ex;
import micro.Hydrator;
import micro.Value;
import micro._Ex;

public final class PropagateValueEvent extends ExEvent {
    public Value value;
    public _Ex to;
    private long exIdTo;

    public PropagateValueEvent() {
    }

    public PropagateValueEvent(long eventId, Ex from, _Ex to, Value value) {
        super(eventId, from);
        this.value = value;
        this.to = to;
        this.exIdTo = to.getId();
    }

    @Override
    public void hydrate(Hydrator h) {
        super.hydrate(h);
        if (to == null){
            to = h.getExForId(exIdTo);
        }
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
}