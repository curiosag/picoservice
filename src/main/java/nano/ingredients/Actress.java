package nano.ingredients;

import akka.actor.ActorRef;
import nano.ingredients.trace.TraceMessage;
import nano.misc.Adresses;

import java.util.Objects;

import static nano.ingredients.Actresses.getStackTrace;
import static nano.ingredients.Actresses.resolve;
import static nano.ingredients.trace.TraceMessage.traced;

public abstract class Actress {

    public ActorRef aref;
    boolean debug = false;
    boolean trace = false;
    boolean idle = false;
    boolean stop = false;

    protected Actress tracer = resolveTracer();

    public ActorRef aRef() {
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
        return idle;
    }

    public void stop() {
        stop = true;
    }

    private long maxMessageId = 0L;

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

    public void tell(Message m) {
        aRef().tell(m, m.origin.sender.aref);
    }

    public void receive(Message m) {
        idle = false;
        try {
            debug(m, m.origin, "!!");
            if (m.key.equals(Name.ack)) {
                onAck((Message) m.value);
            } else {
                process(m);
            }
            if (m.ack == Acknowledge.Y) {
                m.origin.sender.aRef().tell(ackMsg(m), this.aRef());
            }

        } catch (Exception e) {
            Actresses.instance().abortFor(e, this);
        }
        idle = true;
    }

    protected String getExceptionMessage(Message m, Exception e) {
        return this.address + " " + m + "\n" + getStackTrace(e);
    }

    private Message ackMsg(Message m) {
        return new Message(Name.ack, m, m.origin.sender((Function) this));
    }

    protected void onAck(Message value) {
    }

    protected void trace(Message message) {
        if (trace) {
            tracer.aRef().tell(traced(message, this), this.aRef());
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

    protected void log(String s) {
        System.out.println(s);
    }

    public void checkSanityOnStop() {
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
        tracer = resolveTracer();
    }

    @Override
    public String toString() {
        return address.toString();
    }
}
