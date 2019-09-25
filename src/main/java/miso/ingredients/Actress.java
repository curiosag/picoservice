package miso.ingredients;

import akka.actor.ActorRef;
import miso.ingredients.trace.TraceMessage;
import miso.misc.Adresses;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static miso.ingredients.Actresses.resolve;
import static miso.ingredients.trace.TraceMessage.traced;

public abstract class Actress {

    public ActorRef aref;
    boolean debug = false;
    boolean trace = false;
    protected Actress tracer = resolveTracer();

    public ActorRef getAref() {
        return aref;
    }

    public void setAref(ActorRef aref) {
        this.aref = aref;
    }

    protected Actress resolveTracer() {
        return resolve(Adresses.trace);
    }

    public final Address address;

    public boolean idle() {
        return true;
    }

    private AtomicBoolean stopping = new AtomicBoolean(false);

    private long maxMessageId = 0L;

    boolean isStopped() {
        return true;
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
        return address.id + "/" + maxMessageId++;
    }

    protected void debug(Message m, Origin o, String rel) {
        if (!(m instanceof TraceMessage))
            debug(String.format("%s<%d>(%d/%d)%s " + rel + " %s %s", m.origin.callStack.toString(), o.seqNr, o.executionId, o.callStack.size(), this.address.toString(), o.sender.address.toString(), m.toString()));
    }

    public abstract void process(Message message);

    protected void stop() {
        stopping.compareAndSet(false, true);
    }

    public void tell(Message m) {
        getAref().tell(m, m.origin.sender.aref);
    }

    public void receive(Message m) {
        try {
            debug(m, m.origin, "!!");
            if (m.key.equals(Name.ack)) {
                onAck((Message) m.value);
            } else {
                process(m);
            }
            if (m.ack == Acknowledge.Y) {
                if (!(this instanceof Function)) {
                    //TODO: that's messy
                    throw new IllegalStateException();
                }
                m.origin.sender.getAref().tell(ackMsg(m), this.getAref());
            }

        } catch (Exception e) {
            debug(this.address + " " + e.toString());
            throw e;
        }
    }

    private Message ackMsg(Message m) {
        return new Message(Name.ack, m, m.origin.sender((Function) this));
    }

    protected void onAck(Message value) {
    }

    protected void trace(Message message) {
        if (trace) {
            tracer.getAref().tell(traced(message, this), this.getAref());
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
        this.trace = trace;
        tracer = resolveTracer();
    }
}
