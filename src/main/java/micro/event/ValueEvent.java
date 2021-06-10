package micro.event;

import micro.Check;
import micro.Hydrator;
import micro.Value;
import micro._Ex;
import micro.event.serialization.Incoming;
import micro.event.serialization.Outgoing;

public class ValueEvent extends ExEvent {
    public Value value;

    public ValueEvent(_Ex ex, Value value) {
        super(ex);
        this.value = value;
    }

    public ValueEvent() {
    }

    @Override
    public void write(Outgoing output) {
        super.write(output);
        value.write(output);
    }

    @Override
    public void read(Incoming input) {
        super.read(input);
        value = new Value();
        value.read(input);
    }

    @Override
    public void hydrate(Hydrator h) {
        Check.notNull(value);
        super.hydrate(h);
        value.hydrate(h);
    }

    @Override
    public void dehydrate() {
        super.dehydrate();
        value.dehydrate();
    }
}
