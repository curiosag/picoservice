package micro;

import micro.exevent.*;
import micro.serialization.Serialization;
import micro.trace.Tracer;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Env implements Closeable {
    private final Serialization serialization;
    private final int numThreads;

    private final Tracer tracer = new Tracer(false);

    private long maxFId = 0;
    private long maxPropagationId = 0;
    private final AtomicLong maxExId = new AtomicLong(0);
    private final AtomicInteger delay = new AtomicInteger(0);
    private final AtomicBoolean suspend = new AtomicBoolean(false);
    public final List<ExEvent> events = new ArrayList<>();

    private Map<Long, _F> idToF = new HashMap<>();
    private Map<Long, _Ex> idToX = new HashMap<>();

    private final Address address;

    public Env(int numThreads, Address address) {
        this.serialization = new Serialization(this);
        this.address = address;
        this.numThreads = numThreads;
        for (int i = 0; i < numThreads; i++) {
            new Thread(this::processEvents).start();
        }
    }

    public void raiseEvent(EnvEvent e) {
        // persist e
        if (e instanceof EventQueueRemove) {
            EventQueueRemove r = (EventQueueRemove) e;
            events.remove(r.e);
            return;
        }
        Check.fail("unknown event type " + e.toString());
    }

    public void noteEvent(ExEvent e) {
        // persist e
        if (e instanceof ValueReceivedEvent) {
            ValueReceivedEvent v = (ValueReceivedEvent) e;
            log(v.getEx().getLabel() + " " + v.value.getName() + " " + v.value.get().toString());
            events.add(e);
            return;
        }
        if (e instanceof PropagateValueEvent) {
            events.add(0, e);
            return;
        }
        if (e instanceof ValueProcessedEvent) {
            ValueProcessedEvent p = (ValueProcessedEvent) e;
            //events.add(e);
            return;
        }
        Check.fail("unknown event type " + e.toString());
    }

    private void processEvents() {

        while (true) {
            if (suspend.get()) {
                sleep(200);
                continue;
            }
            sleep(delay.get());
            if (events.size() > 0) {
                ExEvent e = events.get(0);
                e.getEx().handle(e);
                raiseEvent(new EventQueueRemove(e));
            } else {
                sleep(100);
            }
        }
    }

    private void sleep(int millis) {
        if (millis > 0)
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
    }

    @Override
    public void close() throws IOException {
        tracer.close();
    }

    _Ex createExecution(_F called, ExFCall returnTo) {
        return called.createExecution(this, returnTo);
    }

    public _Ex createExecution(_F called) {
        ExTop top = new ExTop(this); //todo abandoned top possible
        _Ex result = called.createExecution(this, top);

        return result;
    }

    public Address getAddress() {
        return address;
    }

    public void log(String msg) {
        System.out.println(Thread.currentThread().getId() + " " + msg);
    }

    public void debug(String msg) {
        System.out.println(Thread.currentThread().getId() + " " + msg);
    }


    public _Ex createTop() {
        return new ExTop(this);
    }

    public long nextFPropagationId() {
        return maxPropagationId++;
    }

    void addF(_F f) {
        f.setId(maxFId++);
        idToF.put(f.getId(), f);
    }

    void addX(_Ex x) {
        x.setId(maxExId.getAndIncrement());
        idToX.put(x.getId(), x);
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

}
