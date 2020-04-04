package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Check;
import micro.Hydrator;
import micro.Value;
import micro._Ex;

public class ValueEvent extends ExEvent {
    public Value value;

    public ValueEvent(_Ex ex, Value value) {
        super(ex);
        this.value = value;
    }

    public ValueEvent() {
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        value.write(kryo, output);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        value = new Value();
        value.read(kryo, input);
    }

    @Override
    public void hydrate(Hydrator h) {
        Check.notNull(value);
        super.hydrate(h);
        value.hydrate(h);
    }

    @Override
    public String toString() {
        throw new IllegalStateException();
    }

}
