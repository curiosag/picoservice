package micro;

import micro.event.eventlog.memeventlog.SimpleListEventLog;

import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ReRun {

    private static final Address address = new Address(new byte[0], 1, 1);

    public record InitialRun(Long exId, List<Hydratable> events){}

    public static InitialRun runAndCheck(Object expected, Function<Node, CallSync<?>> call) {
        SimpleListEventLog log = new SimpleListEventLog();
        long exId;
        try (Node node = new Node(address, log, log)) {
            node.start();
            CallSync<?> sync = call.apply(node);
            exId = sync.prepareEx();
            assertEquals(expected, sync.call());
        }
        return new InitialRun(exId, log.events);
    }

    public static void reRunAndCheck(long latchOntoExId, Function<Node, CallSync<?>> call, List<Hydratable> events, Object expected) {
        SimpleListEventLog log =  new SimpleListEventLog(events);
        try (Node node = new Node(address, log, log)) {
            node.start();
            CallSync<?> sync = call.apply(node);
            sync.latchOnto(latchOntoExId);
            assertEquals(expected, sync.call());
        }
    }

}
