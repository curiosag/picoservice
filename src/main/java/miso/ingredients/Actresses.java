package miso.ingredients;

import miso.ingredients.trace.Trace;
import miso.misc.Adresses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Actresses {

    private boolean debug = false;
    private boolean trace = false;

    private Map<String, Actress> addressed = new HashMap<>();

    private List<Actress> cast = new ArrayList<>();
    private Trace tracer;
    private AtomicInteger maxId = new AtomicInteger(0);

    private static Actresses instance;

    public static Actresses instance() {
        if (instance == null) {
            instance = new Actresses();
            instance.resetInstance();
        }
        return instance;
    }


    public static Actress resolve(Address address) {
        return resolve(address.value);
    }

    public static Actress resolve(String address) {
        Actress result = instance().addressed.get(address);
        if (result == null)
            throw new IllegalStateException("not found: " + address);
        return result;
    }

    public void resetInstance() {
        cast.clear();
        addressed.clear();
        debug = false;
        trace = false;
        setUpTracer(trace);
        maxId = new AtomicInteger(0);
    }

    private void setUpTracer(boolean trace) {
        if (!trace && tracer != null) {
            shutdownTracer();
        }
        tracer = new Trace(trace);
        start(tracer);
        cast.remove(tracer); // must be handled seperately
        addressed.put(Adresses.trace, tracer);
    }

    public static void reset() {
        instance().resetInstance();
    }

    public static int nextId() {
        return instance().maxId.addAndGet(1);
    }

    public static void start(Actress a) {
        instance().startActress(a);
    }

    private void startActress(Actress a) {
        addressed.put(a.address.value, a);
        a.trace = trace;
        a.debug = debug;
        new Thread(a).start();
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
        setUpTracer(trace);
        cast.forEach(a -> a.setTrace(trace));
    }

    public void showCast(){
        cast.forEach(c -> System.out.println(c.address.id + " " +  c.address.toString()));
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

    public void forceShutdown(){
        doShutdown();
    }

    private void shutdownInstance() {
        await(() -> cast.stream().allMatch(Actress::idle));
        doShutdown();
    }

    private void doShutdown() {
        cast.forEach(Actress::stop);
        await(() -> cast.stream().allMatch(Actress::isStopped));
        cast.forEach(Actress::checkSanityOnStop);
        shutdownTracer();
    }

    private void shutdownTracer() {
        if (tracer != null) {
            await(() -> tracer.idle());
            tracer.stop();
            try {
                tracer.close(); // trace needs to be available until everything else has stopped
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            addressed.remove(tracer);
            tracer = null;
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
