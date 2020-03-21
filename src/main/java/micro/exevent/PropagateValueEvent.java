package micro.exevent;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Ex;
import micro.Value;
import micro._Ex;

public final class PropagateValueEvent implements ExEvent {
    public final Value value;
    private final Ex from;
    public final _Ex to;

    public PropagateValueEvent(Ex from, _Ex to, Value value) {
        this.value = value;
        this.from = from;
        this.to = to;
    }

    @Override
    public void write(Kryo kryo, Output output) {

    }

    @Override
    public void read(Kryo kryo, Input input) {

    }

    @Override
    public Ex getEx() {
        return from;
    }
}