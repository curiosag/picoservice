package micro.event.eventlog;

import micro.event.Event;

import java.io.Closeable;

public interface EventLogWriter extends Closeable {
    void put(Event e);
}
