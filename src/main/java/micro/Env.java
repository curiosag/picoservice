package micro;

import micro.actor.Message;
import micro.serialization.Serialization;
import micro.trace.Tracer;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Env implements Runnable, Closeable {
    private final Serialization serialization;
    private final int numThreads;
    private final Queue<Message> messages = new ConcurrentLinkedQueue<>();
    private final Tracer tracer = new Tracer(false);

    private long maxFId = 0;
    private final AtomicLong maxExId = new AtomicLong(0);
    private final AtomicInteger delay = new AtomicInteger(0);
    private final AtomicBoolean suspend = new AtomicBoolean(false);

    private Map<Long, _F> idToF = new HashMap<>();
    private Map<Long, _Ex> idToX = new HashMap<>();

    private final Address address;

    public _Ex createTop(){
        return new ExTop(this);
    }

    void addF(_F f){
        f.setId(maxFId++);
        idToF.put(f.getId(), f);
    }

    void addX(_Ex x){
        x.setId(nextId());
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

    long nextId(){
        return maxExId.getAndIncrement();
    }

    public Env(int numThreads, Address address) {
        this.serialization = new Serialization(this);
        this.address = address;
        this.numThreads = numThreads;
        for (int i = 0; i < numThreads; i++) {
            new Thread(this).start();
        }
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

    public void enq(Value v, Ex target) {
        Message m = new Message(v, target);
        tracer.trace(m);
        if (numThreads > 0) {
            messages.add(m);
        } else {
            target.process(v);
        }
    }

    @Override
    public void run() {

        while (true) {
            if (suspend.get()) {
                sleep(200);
                continue;
            }
            sleep(delay.get());
            Message m = messages.poll();
            if (m != null) {
                m.target.process(m.value);
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

    public void registerDone(ExPropagation p) {

        p.setDone(true);
    }
}
