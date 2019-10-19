package nano.ingredients;

import akka.actor.ActorSystem;
import nano.ingredients.akka.Akktor;
import nano.ingredients.infrastructure.Tracer;
import nano.misc.Adresses;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static nano.ingredients.AsyncStuff.await;
import static nano.ingredients.RunMode.TERMINATING;
import static nano.ingredients.RunProperty.TRACE;

public class Ensemble {

    private transient ActorSystem actorSystem = ActorSystem.create("picoservice");

    public final List<Actress> ensemble = new ArrayList<>();

    private final Set<RunProperty> runProperties = new HashSet<>();
    private final Map<Long, Actress> addressed = new HashMap<>();
    private final Map<String, Actress> actressById = new HashMap<String, Actress>();

    private static final long reservedIds = 99;
    private static AtomicLong maxId = new AtomicLong(reservedIds); // tracer, callCreator

    public static Tracer tracer;
    private static Ensemble instance;

    public Ensemble() {

    }

    public static Ensemble instance() {
        if (instance == null) {
            instance = new Ensemble();
            tracer = new Tracer(false);
            instance.wireActress(tracer);
            registerTracer();
        }
        return instance;
    }

    private static void registerTracer() {
        instance.addressed.put(Adresses.trace, tracer);
        instance.actressById.put(tracer.address.id, tracer);
    }

    public void setRunProperty(RunProperty p) {
        runProperties.add(p);
    }

    public void setRunProperties(RunProperty... p) {
        runProperties.clear();
        Collections.addAll(runProperties, p);

        tracer.setTrace(hasRunProperty(TRACE));
        ensemble.forEach(a -> a.setRunProperties(runProperties));
    }

    public boolean hasRunProperty(RunProperty p) {
        return runProperties.contains(p);
    }

    static Actress resolve(Address address) {
        return resolve(address.value);
    }

    static Actress resolve(Long address) {
        if (address.equals(Adresses.trace)) {
            return tracer;
        }
        Actress result = instance().addressed.get(address);
        if (result == null)
            throw new IllegalStateException("not found: " + address);
        return result;
    }

    static Actress resolve(String id) {
        if (id.equals(tracer.address.id)) {
            return tracer;
        }
        Actress result = instance().actressById.get(id);
        if (result == null)
            throw new IllegalStateException("Actress not found for id " + id);
        return result;
    }

    public static void reset() {
        instance().resetInstance();
    }

    static String nextId() {
        return String.valueOf(maxId.addAndGet(1));
    }

    public static void attachActor(Actress a) {
        instance().ensemble.add(instance().wireActress(a));
    }

    private Actress wireActress(Actress a) {
        enlist(a);
        a.setRunProperties(runProperties);

        String id = a.address.id;
        a.setAref(actorSystem.actorOf(Akktor.props(a), id));

        return a;
    }

    void enlist(Actress a) {
        addressed.put(a.address.value, a);
        actressById.put(a.address.id, a);
    }

    public void showEnsemble() {
        ensemble.forEach(c -> System.out.println(c.address.id + " " + c.address.toString()));
    }

    public static void terminate() {
        instance().terminateInstance();
    }

    void abort(Exception e, Actress occuredAt) {
        log("Aborting due to exception in " + occuredAt.address.toString() + '\n' + getStackTrace(e));
        doTerminate();
    }

    private void terminateInstance() {
        doTerminate();
    }

    private void doTerminate() {
        setTerminating();
        await(3000);
        ensemble.forEach(a -> suppressNpe(() -> actorSystem.stop(a.aref)));
        ensemble.forEach(a -> suppressNpe(a::stop));
        // await(1, () -> ensemble.stream().allMatch(Actress::isStopped)); TODO doesn't really work this way, stopped flag never gets set if the actor is idle
        ensemble.forEach(Actress::checkSanityOnStop);
        flushTracer();

    }

    private void setTerminating() {
        try {
            ensemble.forEach(a -> suppressNpe(() -> a.setRunMode(TERMINATING)));
        } catch (ConcurrentModificationException e) {
            setTerminating(); // at some point they don't produce any more actors
        }
    }

    private void suppressNpe(Runnable r) {
        try {
            r.run();
        } catch (Exception e) { // TODO NPEs
            log(e.getClass().getSimpleName() + " on terminateInstance\n" + getStackTrace(e));
        }
    }

    /**
     * this resetInstance is only here to be used for running unittests
     * spares to restart an actor system every time, but is crappy otherwise
     * */
    private void resetInstance() {
        try {
            maxId.set(reservedIds);
            runProperties.clear();
            actressById.clear();
            addressed.clear();
            ensemble.clear();

            //mpf... InvalidActorNameException on terminateInstance
            //akka.actor.InvalidActorNameException: actor name [0] is not unique!
            tracer.address.id = tracer.address.id + 1L;
            tracer.setTrace(false);
            wireActress(tracer);
            registerTracer();
        } catch (Exception e) { // TODO NPEs
            log(e.getClass().getSimpleName() + " on terminateInstance\n" + getStackTrace(e));
            throw e;
        }
    }

    private void log(String s) {
        System.out.println(s);
    }

    static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private void flushTracer() {
        if (tracer != null) {
            //await(() -> tracer.idle());//TODO input queue for tracer
            tracer.stop();
            tracer.flush(); // trace needs to be available until everything else has stopped
        }
    }

}
