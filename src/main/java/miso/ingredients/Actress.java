package miso.ingredients;

import miso.ingredients.trace.Trace;
import miso.misc.Adresses;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static miso.ingredients.Actresses.resolve;
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

    public boolean idle() {
        return inBox.size() == 0 && idle;
    }

    private AtomicBoolean stopping = new AtomicBoolean(false);

    private boolean stopped = true;

    boolean isStopped() {
        return stopped;
    }

    protected Actress(Address address) {
        this.address = address;
    }

    protected Actress() {
        address = new Address(this.getClass().getSimpleName() + "-" + Actresses.nextAddress());
    }

    public void label(String sticker) {
        address.setLabel(sticker);
    }

    public void receive(Message m) {
        if(m.origin.sender.address.toString().contains("filterReCallOnFalse")) {
            debug(String.format("<%d>%s <- %s %s", m.origin.seqNr, this.address.toString(), m.origin.sender.address.toString(), m.toString()));
        }
        debug(String.format("<%d>%s <- %s %s", m.origin.seqNr, this.address.toString(), m.origin.sender.address.toString(), m.toString()));
        inBox.add(m);
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
                 //   if(this.address.toString().contains("filterReCallOnFalse")) {
                        debug(String.format("<%d>%s !! %s %s", m.origin.seqNr, this.address.toString(), m.origin.sender.address.toString(), m.toString()));
                 //   }
                    process(m);
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

    protected void maybeTrace(Message message) {
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
