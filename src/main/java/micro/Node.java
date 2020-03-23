package micro;

import micro.event.*;
import micro.trace.Tracer;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Node implements Closeable, Hydrator {
    private final int numThreads;

    private final Tracer tracer = new Tracer(false);
    private final EventLogWriter eventLog;

    private long maxFId = 0;
    private long maxPropagationId = 0;
    private final AtomicLong maxEventId = new AtomicLong(0);
    private final AtomicLong maxExId = new AtomicLong(0);
    private final AtomicInteger delay = new AtomicInteger(0);
    private final AtomicBoolean suspend = new AtomicBoolean(false);
    public final List<ExEvent> events = new ArrayList<>();

    private Map<Long, _F> idToF = new HashMap<>();
    private Map<Long, _Ex> idToX = new HashMap<>(); //todo works only single threaded now
    private Map<EnvExMatcher, _Ex> exRecovered = new HashMap<>();

    private final Address address;

    public Node(int numThreads, Address address) {
        this.address = address;
        this.numThreads = numThreads;
        recover(address);
        eventLog = new EventLogWriter(getEventLogFileName(address), false);
        for (int i = 0; i < numThreads; i++) {
            new Thread(this::processEvents).start();
        }
    }

    private String getEventLogFileName(Address address){
        return "/home/ssmertnig/temp/" + address.getNode() + ".kryo";
    }

    private void recover(Address address) {
        EventLogReader reader = new EventLogReader(getEventLogFileName(address));
        for(Hydratable h: reader)
        {

        }
    }

    public void raise(NodeEvent e) {
        eventLog.put(e);
        if (e instanceof QueueRemoveEvent) {
            QueueRemoveEvent r = (QueueRemoveEvent) e;
            events.removeIf(i -> i.getId() == r.idEventToRemove);
            return;
        }
        Check.fail("unknown event type " + e.toString());
    }

    public void note(ExEvent e) {
        e.setId(maxEventId.getAndIncrement());
        eventLog.put(e);
        if (e instanceof ValueReceivedEvent) {
            ValueReceivedEvent v = (ValueReceivedEvent) e;
            log((v.getEx() instanceof Ex ? ((Ex) v.getEx()).getLabel() : "") + " " + v.value.getName() + " " + v.value.get().toString());
            events.add(e);
            return;
        }
        if (e instanceof PropagateValueEvent) {
            events.add(0, e);
            return;
        }
        if (e instanceof ValueProcessedEvent) {
            ValueProcessedEvent p = (ValueProcessedEvent) e;
            events.add(e);
            return;
        }
        Check.fail("unknown event type " + e.toString());
    }

    private void processEvents() {

        while (true) {
            if (suspend.get()) {
                Concurrent.sleep(200);
                continue;
            }
            Concurrent.sleep(delay.get());
            if (events.size() > 0) {
                ExEvent e = events.get(0);
                Check.invariant(e.getEx() instanceof Ex, "öh");
                ((Ex) e.getEx()).handle(e);
                raise(new QueueRemoveEvent(e.getId()));
            } else {
                Concurrent.sleep(10);
            }
        }
    }

    @Override
    public void close() {
        tracer.close();
        eventLog.close();
    }

    _Ex getExecution(_F targetFunc, _Ex returnTo) {
        _Ex result = exRecovered.get(new EnvExMatcher(returnTo, targetFunc));
        if (result != null) {
            return result;
        } else {
            return createExecution(targetFunc, returnTo);
        }
    }

    private _Ex createExecution(_F targetFunc, _Ex returnTo) {
        _Ex result = targetFunc.createExecution(this, returnTo);
        result.setId(maxExId.getAndIncrement());
        idToX.put(result.getId(), result);
        if (result.getAddress().nodeEqual(getAddress())) {
            eventLog.put(new ExecutionCreatedEvent(result));
        }
        return result;
    }

    public _Ex getExecution(_F targetFunc) {
        ExTop top = new ExTop(this); //todo abandoned top possible
        return getExecution(targetFunc, top);
    }

    public Address getAddress() {
        return address;
    }

    public void log(String msg) {
        print(msg);
    }

    public void debug(String msg) {
        print(msg);
    }

    private void print(String msg) {
        System.out.println(Thread.currentThread().getId() + " " + msg);
    }

    public _Ex createTop() {
        return new ExTop(this);
    }

    public long nextFPropagationId() {
        return maxPropagationId++;
    }

    void addF(_F f) {
        // todo f's id depend on the sequence of creation, Fs aren't serialized
        f.setId(maxFId++);
        idToF.put(f.getId(), f);
    }

    public int getDelay() {
        return delay.get();
    }

    void setDelay(int delay) {
        this.delay.set(delay);
    }

    public void suspend(boolean suspend) {
        this.suspend.set(suspend);
    }

    @Override
    public _Ex getExForId(long exId) {
        return Check.notNull(idToX.get(exId));
    }

    @Override
    public _F getFForId(long fId) {
        return Check.notNull(idToF.get(fId));
    }
}
