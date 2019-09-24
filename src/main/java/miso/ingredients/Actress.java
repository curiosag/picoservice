package miso.ingredients;

import miso.ingredients.trace.Trace;
import miso.ingredients.trace.TraceMessage;
import miso.misc.Adresses;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static miso.ingredients.Actresses.resolve;
import static miso.ingredients.Message.message;
import static miso.ingredients.trace.TraceMessage.traced;

public abstract class Actress implements Runnable {

    boolean debug = false;
    boolean trace = false;
    boolean idle = true;
    protected Actress tracer = resolveTracer();

    private Actress resolveTracer() {
        if (this instanceof Trace) {
            return this;
        } else {
            return resolve(Adresses.trace);
        }
    }

    public final Address address;
    Queue<Message> inBox = new ConcurrentLinkedQueue<>();
    Queue<String> acknowledged = new ConcurrentLinkedQueue<>(); // of Message.id

    public boolean idle() {
        return inBox.size() == 0 && idle;
    }

    private AtomicBoolean stopping = new AtomicBoolean(false);

    AtomicLong maxMessageId = new AtomicLong(0L);

    private boolean stopped = true;

    boolean isStopped() {
        return stopped;
    }

    protected Actress(Address address) {
        this.address = address;
    }

    protected Actress() {
        address = new Address(this.getClass().getSimpleName(), Actresses.nextId());
    }

    public void label(String sticker) {
        address.setLabel(sticker);
    }

    public String getNextMessageId() {
        return address.id + "/" + maxMessageId.getAndIncrement();
    }

    public void receive(Message m) {
        Origin o = m.origin;
        debug(m, o, "<--");
        if (m.key.equals(Name.ack)) {
            acknowledged.add((String) m.value);
        } else {
            inBox.add(m);
        }
    }

    protected void debug(Message m, Origin o, String rel) {
        if (!(m instanceof TraceMessage))
            debug(String.format("%s<%d>(%d/%d)%s " + rel + " %s %s", m.origin.callStack.toString(), o.seqNr, o.executionId, o.callStack.size(), this.address.toString(), o.sender.address.toString(), m.toString()));
    }

    protected abstract void process(Message message);

    protected void stop() {
        stopping.compareAndSet(false, true);
    }

    @Override
    public void run() {
        stopped = false;
        stopping.set(false);
        idle = true;

        while (!stopping.get())
            try {
                Message m = inBox.poll();
                if (m != null) {
                    idle = false;
                    debug(m, m.origin, "!!");
                    process(m);
                    if (m.ack == Acknowledge.Y) {
                        if (!(this instanceof Function)) {
                            //TODO: that's messy
                            throw new IllegalStateException();
                        }
                        m.origin.sender.receive(message(Name.ack, m.id, m.origin.sender((Function) this)));
                    }
                } else {
                    idle = true;
                    Thread.yield();
                }

            } catch (Exception e) {
                debug(this.address + " " + e.toString());
                throw e;
            }
        stopped = true;
    }

    protected void trace(Message message) {
        if (trace) {
            tracer.receive(traced(message, this));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Actress actress = (Actress) o;
        return Objects.equals(address, actress.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    protected void debug(String s) {
        if (debug) {
            System.out.println(s);
        }
    }

    public void checkSanityOnStop() {
    }

    public void setTrace(boolean trace) {
        tracer = resolveTracer();
    }
}
