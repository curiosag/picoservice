package micro.event;

import com.esotericsoftware.kryo.Kryo;
import micro.Hydratable;

import static micro.event.KryoStuff.createInput;

public class EventLogReader implements Iterable<Hydratable> {
    Kryo kryo = new Kryo();

    private final String filename;

    public EventLogReader(String filename) {
        this.filename = filename;
    }

    @Override
    public EventLogIterator iterator() {
        return new EventLogIterator(kryo, createInput(filename));
    }

}
