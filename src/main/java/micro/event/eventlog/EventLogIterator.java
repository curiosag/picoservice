package micro.event.eventlog;

import com.esotericsoftware.kryo.KryoException;
import micro.Check;
import micro.Hydratable;
import micro.event.serialization.Incoming;
import micro.event.serialization.SerioulizedEvent;
import micro.event.serialization.Serioulizable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public class EventLogIterator implements Iterator<Hydratable>, Closeable {

    private final Incoming input;
    private Hydratable next;

    public EventLogIterator(Incoming input) {
        this.input = input;
        next = readNext();
    }

    private Hydratable readNext() {
        try {
            Serioulizable result = SerioulizedEvent.readObject(input);
            Check.invariant(result instanceof Hydratable, "hm...");
            //noinspection ConstantConditions
            return (Hydratable) result;
        } catch (KryoException e) {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Hydratable next() {
        Check.invariant(next != null, "no more events");
        Hydratable result = next;
        next = readNext();
        return result;
    }

    @Override
    public void close() {
        try {
            input.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
