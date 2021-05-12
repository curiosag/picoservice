package micro.event.eventlog;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import micro.Check;
import micro.Hydratable;

public class KryoEventLogIterator implements EventLogIterator {

    private final Kryo kryo;
    private final Input input;
    private Hydratable next;

    KryoEventLogIterator(Kryo kryo, Input input) {
        this.kryo = kryo;
        this.input = input;
        next = readNext();
    }

    private Hydratable readNext() {
        try {
            KryoSerializable result = KryoSerializedClass.readObject(kryo, input);
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
        input.close();
    }
}
