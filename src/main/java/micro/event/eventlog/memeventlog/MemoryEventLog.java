package micro.event.eventlog.memeventlog;

import micro.Hydratable;
import micro.event.Event;
import micro.event.eventlog.EventLogIterator;
import micro.event.eventlog.EventLogReader;
import micro.event.eventlog.EventLogWriter;
import micro.event.serialization.SerioulizedEvent;
import micro.event.serialization.MemIncoming;
import micro.event.serialization.MemOutgoing;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public class MemoryEventLog implements EventLogReader, EventLogWriter {

    Queue<Object> values = new ArrayDeque<>();
    MemOutgoing out = new MemOutgoing(values);
    MemIncoming in = new MemIncoming(values);

    @Override
    public void put(Event e) {
        SerioulizedEvent.writeObject(out, e);
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator<Hydratable> iterator() {
        return new EventLogIterator(in);
    }
}
