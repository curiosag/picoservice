package micro.event.eventlog;

import micro.Hydratable;
import micro.event.Event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MemoryEventLog implements EventLogReader, EventLogWriter{

    List<Hydratable> events = new ArrayList<>();

    @Override
    public void put(Event e) {
        events.add(e);
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator<Hydratable> iterator() {
        return events.iterator();
    }
}
