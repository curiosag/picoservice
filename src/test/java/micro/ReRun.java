package micro;

import micro.event.eventlog.memeventlog.SimpleListEventLog;
import micro.gateway.Gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ReRun {

    private static final Address address = new Address(new byte[0], 1, 1);

    public record InitialRun(Long exId, List<Hydratable> events) {
    }

    public static InitialRun runAndCheck(Object expected, Function<Env, Gateway<?>> call) {
        SimpleListEventLog log = new SimpleListEventLog();
        long exId;
        try (Node env = new Node(address, log, log)) {
            env.start();
            Gateway<?> sync = call.apply(env);
            exId = sync.getId();
            assertEquals(expected, sync.call());
        }
        return new InitialRun(exId, log.events);
    }

    public static void reRunAndCheck(long latchOntoExId, BiFunction<Long, Env, Gateway<?>> call, List<Hydratable> events, Object expected) {
        SimpleListEventLog log = new SimpleListEventLog(events);
        try (Node env = new Node(address, log, log)) {
            Gateway<?> sync = call.apply(latchOntoExId, env);
            env.start(true); //TODO race condition, more if node started before call.apply. Node.idToF.get(fId) returns null for fid 1

            assertEquals(expected, sync.call());
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void reReReReRunAndCheck(long latchOntoExId, BiFunction<Long, Env, Gateway<?>> call, List<Hydratable> events, Object expected) {
        if (true)
            for (int i = events.size(); i > 2; i--) {
                ArrayList<Hydratable> useEvents = new ArrayList<>(events.subList(0, i));
                ReRun.reRunAndCheck(latchOntoExId, call, useEvents, expected);
                if (i % 10 == 0) {
                    System.out.print("\n");
                }
                System.out.printf("[%d]", i);
                System.out.print(" ");
            }
        else {
            ArrayList<Hydratable> useEvents = new ArrayList<>(events.subList(0, 32));
            ReRun.reRunAndCheck(latchOntoExId, call, useEvents, expected);
        }
        System.out.print("\n");

    }

}
