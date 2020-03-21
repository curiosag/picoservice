package micro;

import micro.exevent.ExEvent;
import micro.exevent.ValueReceivedEvent;
import micro.exevent.ValueProcessedEvent;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

class EnvEventHandler {

    public final Queue<ExEvent> events = new ArrayDeque<>();

    private final Map<Class, Consumer<ExEvent>> handlers = new HashMap<>();

    EnvEventHandler() {
        handlers.put(ValueReceivedEvent.class, e -> hdl((ValueReceivedEvent) e));
        handlers.put(ValueProcessedEvent.class, e -> hdl((ValueProcessedEvent) e));
    }

    private void hdl(ValueReceivedEvent e) {
    }

    private void hdl(ValueProcessedEvent e) {

    }

    void hdl(ExEvent e) {
        Consumer<ExEvent> handler = handlers.get(e.getClass());
        if(handler == null)
        {
            Check.fail("no event handler for event " + e.toString());
        }
        handler.accept(e);
    }

    private void persist(ExEvent e)
    {

    }

}
