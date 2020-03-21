package micro.exevent;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Ex;
import micro.ExPropagation;
import micro.Value;

public final class PropagateValueEvent implements ExEvent {
    public final Value value;
    private final Ex from;
    public final ExPropagation propagation;

    public PropagateValueEvent(Ex from, Value value, ExPropagation propagation) {
        this.value = value;
        this.from = from;
        this.propagation = propagation;
    }

    public static PropagateValueEvent of(Ex from, Value value, ExPropagation propagation) {
        return new PropagateValueEvent(from, value, propagation);
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