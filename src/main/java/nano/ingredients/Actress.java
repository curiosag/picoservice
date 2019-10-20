package nano.ingredients;

import akka.actor.ActorRef;
import nano.ingredients.infrastructure.TraceMessage;
import nano.ingredients.infrastructure.Tracer;
import nano.ingredients.tuples.ReplayData;
import nano.misc.Adresses;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static nano.ingredients.Ensemble.getStackTrace;
import static nano.ingredients.Ensemble.resolve;
import static nano.ingredients.RunProperty.*;
import static nano.ingredients.infrastructure.TraceMessage.traced;

public abstract class Actress implements Serializable {
    private static final long serialVersionUID = 0L;
    private static Logger logger = Logger.getLogger("");
    ActorRef aref;
    private Set<RunProperty> runProperties = new HashSet<>();

    private boolean stop = false;

    private boolean stopped = false;

    private transient Tracer tracer;

    private ReplayData replayData;

    public void setReplayData(ReplayData replayData) {
        this.replayData = replayData;
    }

    protected ReplayData getReplayData() {
        if (replayData == null) {
            throw new IllegalStateException();
        }
        return replayData;
    }

    protected ActorRef aRef() {
        return aref;
    }

    void setAref(ActorRef aref) {
        this.aref = aref;
    }

    protected Tracer resolveTracer() {
        return (Tracer) resolve(Adresses.trace);
    }

    public final Address address;


    void setRunMode(RunProperty runMode) {
        this.runProperties.add(runMode);
    }

    public void stop() {
        stop = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    protected Actress(Address address) {
        this.address = address;
    }

    Actress() {
        address = createAddress();
    }

    Address createAddress() {
        return new Address(-1L, Ensemble.nextId());
    }

    public void label(String sticker) {
        address.setLabel(sticker);
    }

    private void debug(Message m, Origin o, String rel) {
        if (!(m instanceof TraceMessage))
            debug(String.format("%s (%d)%s " + rel + " %s %s", m.origin.getComputationPath().getStack().toString(), o.getComputationPath().getStack().size(), this.getClass().getSimpleName() + this.address.toString(), o.getSender().getClass().getSimpleName() + o.getSender().address.toString(), m.toString()));
    }

    public abstract void process(Message message);

    public void tell(Message m) {
        aRef().tell(m, m.origin.getSender().aref);
    }

    private String getReturnKey() {
        return ((Function) this).returnKey;
    }

    public void receive(Message m) {
        try {
            debug(m, m.origin, "!!");
            if (hasRunProperty(SHOW_STACKS)) {
                System.out.println(m.origin.getComputationPath().getStack().stackPoints());
            }
            if (!m.isRecovered() && (m.key.equals(Name.stackFrame) || m.key.equals(getReturnKey()))) {
                Map<String, Message> repl = getReplayData().getRecovered().get(m.origin.getComputationPath().getStack());
                if (repl != null && repl.get(m.key) != null) {
                    return;
                }
            }
            if (m.key.equals(Name.ack)) {
                onAck((Message) m.getValue());
            } else {
                process(m);
            }
            if (m.ack == Acknowledge.Y) {
                m.origin.getSender().aRef().tell(ackMsg(m), this.aRef());
            }

        } catch (Exception e) {
            Ensemble.instance().abort(e, this);
        }
        stopped = stop;
    }

    String getExceptionMessage(Message m, Exception e) {
        return this.address + " " + m + "\n" + getStackTrace(e);
    }

    private Message ackMsg(Message m) {
        return new Message(Name.ack, m, m.origin.sender((Function) this));
    }

    protected void onAck(Message value) {
    }

    protected void trace(Message message) {
        if (hasRunProperty(TRACE)) {
            try {
                getTracer().aRef().tell(traced(message, this), this.aRef());
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    void debug(String s) {
        if (hasRunProperty(DEBUG)) {
            //logger.debug(s);
            System.out.println(s);
        }
    }

    public void checkSanityOnStop() {
    }

    void setRunProperties(Set<RunProperty> value) {
        this.runProperties = value;
    }

    @Override
    public String toString() {
        return address.toString();
    }

    public void receiveRecover(Message m) {
    }

    public boolean shouldPersist(Message m) {
        return false;
    }

    private boolean hasRunProperty(RunProperty p) {
        return runProperties.contains(p);
    }

    public Tracer getTracer() {
        if (this instanceof Tracer) {
            throw new IllegalStateException();
        }
        if (tracer == null) {
            tracer = resolveTracer();
        }
        return tracer;
    }

}
