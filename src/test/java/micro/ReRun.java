package micro;

import micro.event.eventlog.memeventlog.SimpleListEventLog;
import micro.gateway.CallSync;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ReRun {

    private static final Address address = new Address(new byte[0], 1, 1);

    public record InitialRun(Long exId, List<Hydratable> events) {
    }

    public static InitialRun runAndCheck(Object expected, Function<Env, CallSync<?>> call) {
        SimpleListEventLog log = new SimpleListEventLog();
        long exId;
        try (Node env = new Node(address, log, log)) {
            env.start();
            CallSync<?> sync = call.apply(env);
            exId = sync.prepareEx();
            assertEquals(expected, sync.call());
        }
        return new InitialRun(exId, log.events);
    }

    public static void reRunAndCheck(long latchOntoExId, Function<Env, CallSync<?>> call, List<Hydratable> events, Object expected) {
        SimpleListEventLog log = new SimpleListEventLog(events);
        try (Node env = new Node(address, log, log)) {
            CallSync<?> sync = call.apply(env);
            sync.latchOnto(latchOntoExId);
            env.start(true); //TODO race condition, more if node started before call.apply. Node.idToF.get(fId) returns null for fid 1
            assertEquals(expected, sync.call());
        }
    }

    public static void reReReReRunAndCheck(long latchOntoExId, Function<Env, CallSync<?>> call, List<Hydratable> events, Object expected) {
        if (true)
        for (int i = events.size(); i > 2; i--) {
            //System.out.println("RERUN " + i);
            ArrayList<Hydratable> useEvents = new ArrayList<>(events.subList(0, i));
            ReRun.reRunAndCheck(latchOntoExId, call, useEvents, expected);
            if (i % 10 == 0) {
                System.out.print("\n");
            }
            System.out.print(i);
            System.out.print(" ");
            //System.out.println("RERUN " + i + " DONE");
        } else
        {
            ArrayList<Hydratable> useEvents = new ArrayList<>(events.subList(0, 29));
            ReRun.reRunAndCheck(latchOntoExId, call, useEvents, expected);
        }
        System.out.print("\n");

    }

}
