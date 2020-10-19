package micro;

import micro.event.*;
import micro.trace.Tracer;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Node implements Closeable, Hydrator {
    private static final int NUM_EXEVENTQUEUES = 1;

    private final Tracer tracer = new Tracer(false);
    private final EventLogWriter eventLog;

    private final _Ex top;

    private long nextFId = ExTop.TOP_ID + 1;
    private final AtomicLong nextObjectId = new AtomicLong(ExTop.TOP_ID + 1);

    private final AtomicInteger delay = new AtomicInteger(0);
    private final AtomicBoolean stop = new AtomicBoolean(true);
    private final List<ExEvent>[] events = (List<ExEvent>[]) new List[NUM_EXEVENTQUEUES];

    private Map<Long, _F> idToF = new HashMap<>();
    private Map<Long, _Ex> idToX = new HashMap<>(); //todo works only single threaded now
    private Map<ExFMatcher, _Ex> exRecovered = new HashMap<>();

    private final Address address;

    public Node(Address address, boolean clearEventLog) {
        this.address = address;
        eventLog = new EventLogWriter(getEventLogFileName(address), clearEventLog);
        top = createTop();
        for (int i = 0; i < NUM_EXEVENTQUEUES; i++) {
            events[i] = NUM_EXEVENTQUEUES == 1 ? new ArrayList<>() : Collections.synchronizedList(new ArrayList<>());
        }
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
                    _Ex ex = template.createExecution(retTo);
                    ex.setId(e.exId);
                    maxOId = Math.max(maxOId, e.exId);
                    exRecovered.put(new ExFMatcher(retTo, template), ex);
                    idToX.put(e.exId, ex);
                    break;

                case PropagateValueEvent:
                    h.hydrate(this);
                    enqueue((PropagateValueEvent) h);
                    break;
                case ValueReceivedEvent:
                    h.hydrate(this);
                    enqueue((ValueReceivedEvent) h);
                    break;
                case ValueProcessedEvent:
                    h.hydrate(this);
                    ValueProcessedEvent v = (ValueProcessedEvent) h;
                    Check.invariant(v.ex instanceof Ex, "no ex");
                    ((Ex) v.ex).recover(v.value);
                    enqueue(v);
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
        enqueue(e);
    }

    private void handle(NodeEvent e) {
        if (e instanceof QueueRemoveEvent) {
            QueueRemoveEvent r = (QueueRemoveEvent) e;
            getExEventQueue(r.idRelatedExecution).removeIf(i -> i.getId() == r.idEventToRemove);
            return;
        }
        Check.fail("unknown event type " + e.toString());
    }


    private void enqueue(ExEvent e) {
        if (e instanceof ValueReceivedEvent) {
            ValueReceivedEvent v = (ValueReceivedEvent) e;
            logValueReceived(v);
            getExEventQueue(e.getEx().getId()).add(e);
            return;
        }
        if (e instanceof PropagateValueEvent) {
            getExEventQueue(e.getEx().getId()).add(0, e);
            return;
        }
        if (e instanceof ValueProcessedEvent) {
            getExEventQueue(e.getEx().getId()).add(e);
            return;
        }
        Check.fail("unknown event type " + e.toString());
    }

    private void logValueReceived(ValueReceivedEvent v) {

        String to = null;
        String from = null;
        try {
            to = (v.getEx() instanceof Ex ? ((Ex) v.getEx()).getLabel() : "") + " " + v.ex.getId();
            _Ex sender = v.value.getSender();
            from = sender == null ? "null" : (v.value.getSender().toString() + " " + v.value.getSender().getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        log(Thread.currentThread().getId() + " Rec: " + from + " -> " + to + ": " + v.value.getName() + " " + v.value.get().toString());
    }

    private void processEvents(List<ExEvent> events) {

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
                raise(new QueueRemoveEvent(e.getId(), e.ex.getId()));
            } else {
                Thread.yield();
            }
        }
    }

    @Override
    public void close() {
        tracer.close();
        eventLog.close();
    }

    _Ex getExecution(_F targetFunc, _Ex returnTo) {
        _Ex result = exRecovered.get(new ExFMatcher(returnTo, targetFunc));
        if (result != null) {
            return result;
        } else {
            return createExecution(targetFunc, returnTo);
        }
    }

    private _Ex createExecution(_F targetFunc, _Ex returnTo) {
        _Ex result = targetFunc.createExecution(returnTo);
        result.setId(nextObjectId.getAndIncrement());
        idToX.put(result.getId(), result);
        if (result.getAddress().nodeEqual(getAddress())) {
            eventLogPut(new ExecutionCreatedEvent(result));
        }
        return result;
    }

    private void eventLogPut(Event e) {
        eventLog.put(e);
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
        // todo f's id depends on the sequence of creation, Fs aren't serialized
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
        return Check.notNull(idToX.get(exId));
    }

    @Override
    public _F getFForId(long fId) {
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
        for (int i = 0; i < NUM_EXEVENTQUEUES; i++) {
            final List<ExEvent> queue = events[i];
            new Thread(() -> processEvents(queue)).start();
        }
    }

    private List<ExEvent> getExEventQueue(Long exId) {
        long id = exId >= 0 ? exId : 0;
        return events[Math.toIntExact(id % NUM_EXEVENTQUEUES)];
    }

    void recover() {
        recover(address);
    }
}
