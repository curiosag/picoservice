package micro.event.eventlog;

import com.esotericsoftware.kryo.Kryo;

import static micro.event.eventlog.KryoStuff.createInput;

public class KryoEventLogReader implements EventLogReader {
    Kryo kryo = new Kryo();

    private final String filename;

    public KryoEventLogReader(String filename) {
        this.filename = filename;
    }

    @Override
    public EventLogIterator iterator() {
        return new KryoEventLogIterator(kryo, createInput(filename));
    }

}
