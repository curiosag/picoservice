package micro;

import micro.actor.Message;
import micro.trace.Tracer;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Env implements Runnable, Closeable {

    private final int numThreads;
    private final Queue<Message> messages = new ConcurrentLinkedQueue<>();
    private final Tracer tracer = new Tracer(false);

    private final AtomicLong maxId = new AtomicLong(0);
    private final AtomicInteger delay = new AtomicInteger(0);
    private final AtomicBoolean suspend = new AtomicBoolean(false);

    private Map<Long, Id> items = new HashMap<>();

    private final Address address;

    public void enlist(Id i){
        i.setId(nextId());
        items.put(i.getId(), i);
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
        return maxId.getAndIncrement();
    }

    public Env(int numThreads, Address address) {
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

    _Ex createExecution(F called, ExFCall returnTo) {
        return called.createExecution(this, returnTo);
    }
}
