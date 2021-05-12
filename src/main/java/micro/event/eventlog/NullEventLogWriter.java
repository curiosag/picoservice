package micro.event.eventlog;

import micro.event.Event;

import java.io.IOException;

public class NullEventLogWriter implements EventLogWriter{

    public static NullEventLogWriter instance = new NullEventLogWriter();

    @Override
    public void put(Event e) {

    }

    @Override
    public void close() throws IOException {

    }
}
