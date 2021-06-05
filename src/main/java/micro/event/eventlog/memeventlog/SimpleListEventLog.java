package micro.event.eventlog.memeventlog;

import micro.Check;
import micro.Hydratable;
import micro.event.Event;
import micro.event.eventlog.EventLogReader;
import micro.event.eventlog.EventLogWriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleListEventLog implements EventLogReader, EventLogWriter {

    public final List<Hydratable> events ;

    public SimpleListEventLog() {
        events = new ArrayList<>();
    }

    public SimpleListEventLog(List<Hydratable> events) {
        this.events = events;
    }

    @Override
    public void put(Event e) {
        events.add(e);
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator<Hydratable> iterator() {
        events.forEach(Hydratable::dehydrate);
        return events.iterator();
    }

    public Hydratable dropLast(){
        Check.invariant(events.size() > 0);
        Hydratable result = events.get(events.size() - 1);
        events.remove(result);
        return result;
    }
}
