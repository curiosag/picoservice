package miso.ingredients;

import akka.actor.ActorSystem;
import miso.ingredients.akka.Akktor;
import miso.ingredients.trace.Trace;
import miso.misc.Adresses;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static akka.pattern.Patterns.gracefulStop;

public class Actresses {
    private boolean debug = false;
    private boolean trace = false;

    private final Map<String, Actress> addressed = new HashMap<>();
    private final List<Actress> cast = new ArrayList<>();
    private final ActorSystem akka = ActorSystem.create("microservice");

    private Trace tracer;
    private static AtomicInteger maxId = new AtomicInteger(1); // 0 is the tracer

    private static Actresses instance;

    public Actresses() {

    }

    public static Actresses instance() {
        if (instance == null) {
            instance = new Actresses();

            Trace t = new Trace(false);
            instance.tracer = t;
            instance.addressed.put(Adresses.trace, t);
            instance.wire(t);
            instance.resetInstance();

        }
        return instance;
    }

    public static Actress resolve(Address address) {
        return resolve(address.value);
    }

    public static Actress resolve(String address) {
        if (address.equals(Adresses.trace))
        {
            return instance().tracer;
        }
        Actress result = instance().addressed.get(address);
        if (result == null)
            throw new IllegalStateException("not found: " + address);
        return result;
    }

    public void resetInstance() {
        cast.removeIf(c -> ! c.address.value.equals(Adresses.trace));
        List<String> toRemove = new ArrayList<>(addressed.keySet());
        toRemove.remove(Adresses.trace);
        toRemove.forEach(addressed::remove);

        debug = false;
        tracer.setTrace(false);
    }

    public static void reset() {
        instance().resetInstance();
    }

    public static int nextId() {
        return instance().maxId.addAndGet(1);
    }

    public static void wire(Actress a) {
        instance().wireActress(a);
    }

    private void wireActress(Actress a) {
        addressed.put(a.address.value, a);
        a.trace = trace;
        a.debug = debug;

        String id = a.address.id.toString();
        a.setAref(akka.actorOf(Akktor.props(a), id));

        cast.add(a);
    }

    public static void cleanupFunctions(Long runId) {
        instance().cast.forEach(c -> {
            if (c instanceof Function) {
                ((Function) c).cleanup(runId);
            }
        });
    }

    private static void setDebug(boolean what) {
        instance().debug = what;
        instance().cast.forEach(a -> a.debug = instance().debug);
    }

    private void setTrace(boolean what) {
        trace = what;
        tracer.setTrace(trace);
        cast.forEach(a -> a.setTrace(trace));
    }

    public void showCast() {
        cast.forEach(c -> System.out.println(c.address.id + " " + c.address.toString()));
    }

    public static void trace() {
        instance().setTrace(true);
    }

    public static void noTrace() {
        instance().setTrace(false);
    }

    public static void debug() {
        instance().debug = true;
    }

    public static void noDebug() {
        instance().debug = false;
    }

    public static void shutdown() {
        instance().shutdownInstance();
    }

    public void forceShutdown() {
        doShutdown();
    }

    private void shutdownInstance() {
        await(() -> cast.stream().allMatch(Actress::idle));
        doShutdown();
    }

    private void doShutdown() {
        cast.forEach(Actress::stop);
        await(() -> cast.stream().allMatch(Actress::isStopped));
        cast.forEach(c -> {
            c.checkSanityOnStop();
            gracefulStop(c.getAref(), Duration.ofSeconds(10L));
        });
        shutdownTracer();
    }

    private void shutdownTracer() {
        if (tracer != null) {
            await(() -> tracer.idle());
            tracer.stop();
            tracer.flush(); // trace needs to be available until everything else has stopped
        }
    }

    public static void await(Supplier<Boolean> condition) {
        while (!condition.get()) {
            await(10);
        }
    }

    public static void await(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //
        }
    }
}
