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

    private final Tracer tracer = new Tracer(false);
    private final EventLogWriter eventLog;

    private final _Ex top;

    private long nextFId = ExTop.TOP_ID + 1;
    private final AtomicLong nextObjectId = new AtomicLong(ExTop.TOP_ID + 1);

    private final AtomicInteger delay = new AtomicInteger(0);
    private final AtomicBoolean stop = new AtomicBoolean(true);
    private final List<ExEvent> events = new ArrayList<>();

    private Map<Long, _F> idToF = new HashMap<>();
    private Map<Long, _Ex> idToX = new HashMap<>(); //todo works only single threaded now
    private Map<EnvExMatcher, _Ex> exRecovered = new HashMap<>();

    private final Address address;

    public Node(Address address) {
        this.address = address;
        eventLog = new EventLogWriter(getEventLogFileName(address), false);
        top = createTop();
    }

    private String getEventLogFileName(Address address) {
        return "/home/ssmertnig/temp/" + address.getNode() + ".kryo";
    }

    private void recover(Address address) {
        long maxOId = 0;
         for (Hydratable h : new EventLogReader(getEventLogFileName(address))) {
            KryoSerializedClass k = KryoSerializedClass.forClass(h.getClass());
            switch (k) {
                case QueueRemoveEvent:
                    handle((QueueRemoveEvent) h);
                    break;

                case ExecutionCreatedEvent:
                    ExecutionCreatedEvent e = (ExecutionCreatedEvent) h;
                    _F template = getFForId(e.templateId);
                    _Ex retTo = getExForId(e.exIdToReturnTo);
                    _Ex hydrated = template.createExecution(this, retTo);
                    hydrated.setId(e.exId);
                    maxOId = Math.max(maxOId, e.exId);
                    exRecovered.put(new EnvExMatcher(retTo, template), hydrated);
                    idToX.put(e.exId, hydrated);
                    break;

                case PropagateValueEvent:
                     h.hydrate(this);
                     handle((PropagateValueEvent) h);
                    break;
                case ValueReceivedEvent:
                     h.hydrate(this);
                     handle((ValueReceivedEvent) h);
                    break;
                case ValueProcessedEvent:
                     h.hydrate(this);
                     handle((ValueProcessedEvent) h);
                    break;
                default:
                    throw new IllegalStateException("invalid case");
            }
        }
        nextObjectId.set(maxOId + 1);
    }

    private void raise(NodeEvent e) {
        e.setId(0);
        eventLogPut(e);
        handle(e);
    }

    void note(ExEvent e) {
        if (e.getId() < 0) {
            e.setId(nextObjectId.getAndIncrement());
        }
        eventLogPut(e);
        handle(e);
    }

    private void handle(NodeEvent e) {
        if (e instanceof QueueRemoveEvent) {
            QueueRemoveEvent r = (QueueRemoveEvent) e;
            events.removeIf(i -> i.getId() == r.idEventToRemove);
            return;
        }
        Check.fail("unknown event type " + e.toString());
    }

    private void handle(ExEvent e) {
        if (e instanceof ValueReceivedEvent) {
            ValueReceivedEvent v = (ValueReceivedEvent) e;
            logValueReceived(v);
            events.add(e);
            return;
        }
        if (e instanceof PropagateValueEvent) {
            //logPropagateValue((PropagateValueEvent) e);
            events.add(0, e);
            return;
        }
        if (e instanceof ValueProcessedEvent) {
            //logPropagateValue((ValueProcessedEvent) e);
            events.add(e);
            return;
        }
        Check.fail("unknown event type " + e.toString());
    }

    private void logValueReceived(ValueReceivedEvent v) {
        if(! v.value.getName().startsWith("sorted"))
        {
            return;
        }
        String to = (v.getEx() instanceof Ex ? ((Ex) v.getEx()).getLabel() : "") + " " + v.ex.getId();
        String from = v.value.getSender().toString() + " " + v.value.getSender().getId();

        log("Rec: " +  from + " -> " + to + ": " + v.value.getName() + " " + v.value.get().toString());
    }

    private void logPropagateValue(ValueProcessedEvent v) {
        if (v.getEx().getId() != 100L)
        {
            return;
        }
        String to = (v.getEx() instanceof Ex ? ((Ex) v.getEx()).getLabel() : "") + " " + v.ex.getId();

        log( "Del: -> " + to + ": " + v.valueName);
    }

    private void logPropagateValue(PropagateValueEvent v) {
        if (v.getEx().getId() != 100L)
        {
            return;
        }
        String to = (v.getEx() instanceof Ex ? ((Ex) v.getEx()).getLabel() : "") + " " + v.ex.getId();
        String from = v.value.getSender().toString() + " " + v.value.getSender().getId();

        log( "Prp: " + from + " -> " + to + ": " + v.value.getName() + " " + v.value.get().toString());
    }

    private void processEvents() {

        while (true) {
            if (stop.get()) {
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
        result.setId(nextObjectId.getAndIncrement());
        idToX.put(result.getId(), result);
        if (result.getAddress().nodeEqual(getAddress())) {
            eventLogPut(new ExecutionCreatedEvent(result));
        }
        return result;
    }

    private void eventLogPut(Event e) {
 //       eventLog.put(e);
    }

    _Ex getExecution(_F targetFunc) {
        return getExecution(targetFunc, top);
    }

    public Address getAddress() {
        return address;
    }

    void log(String msg) {
        System.out.println(Thread.currentThread().getId() + " " + msg);
    }

    _Ex getTop() {
        return top;
    }

    long getNextFId() {
        return nextFId++;
    }

    public long getNextObjectId() {
        return nextObjectId.getAndIncrement();
    }

    void addF(_F f) {
        // todo f's id depend on the sequence of creation, Fs aren't serialized
        idToF.put(f.getId(), f);
    }

    void setDelay(@SuppressWarnings("SameParameterValue") int delay) {
        this.delay.set(delay);
    }

    @SuppressWarnings("unused")
    public void stop() {
        this.stop.set(true);
    }

    @Override
    public _Ex getExForId(long exId) {
        _Ex ret = idToX.get(exId);
        if (ret == null) {
            throw new IllegalStateException();
        }
        return Check.notNull(ret);
    }

    @Override
    public _F getFForId(long fId) {
        _F result = idToF.get(fId);
        if (result == null) {
            result = null;
        }
        return Check.notNull(idToF.get(fId));
    }

    private _Ex createTop() {
        _Ex result = new ExTop(address);
        idToF.put(ExTop.TOP_ID, result.getTemplate());
        idToX.put(ExTop.TOP_ID, result);
        return result;
    }

    void start() {
        this.stop.set(false);
        new Thread(this::processEvents).start();
    }

    void recover() {
        recover(address);
    }
}
