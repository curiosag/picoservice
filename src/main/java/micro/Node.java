package micro;

import micro.event.*;
import micro.trace.Tracer;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Node implements Closeable, Hydrator {

    /*

    external    node    execution



    ex' -> CALL receive -> ex -> RECEIVE -> node -> ProcessValueEvent -> ex -> ...
           ...


    * */

    private static final int NUM_EXEVENTQUEUES = 1;

    private final Tracer tracer = new Tracer(false);
    private final EventLogWriter eventLog;

    private final _Ex top;

    private final AtomicInteger delay = new AtomicInteger(0);
    private final AtomicBoolean stop = new AtomicBoolean(true);
    private final List<ExEvent>[] events = (List<ExEvent>[]) new List[NUM_EXEVENTQUEUES];

    private Map<Long, _F> idToF = new HashMap<>();
    private Map<Long, _Ex> idToX = new HashMap<>(); //todo works only single threaded now
    private Map<ExFMatcher, _Ex> exRecovered = new HashMap<>();

    private final Address address;

    public Node(Address address, boolean clearEventLog) {
        IdType.F.next(); // 0 is reserved for TOP
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
        long maxEventId = 0;
        long maxExId = 0;
        for (Hydratable h : new EventLogReader(getEventLogFileName(address))) {
            KryoSerializedClass k = KryoSerializedClass.forClass(h.getClass());
            maxEventId = Math.max(maxEventId, h.getId());
            switch (k) {
                case QueueRemoveEvent:
                    handle((QueueRemoveEvent) h);
                    break;

                case ExecutionCreatedEvent:
                    ExecutionCreatedEvent e = (ExecutionCreatedEvent) h;
                    _F template = getFForId(e.templateId);
                    _Ex retTo = getExForId(e.exIdToReturnTo);
                    _Ex ex = template.createExecution(this, retTo);
                    ex.setId(e.exId);
                    maxExId = Math.max(maxExId, e.exId);
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
        IdType.EX.setNext(maxExId + 1);
        IdType.EVENT.setNext(maxEventId + 1);
    }

    private void raise(NodeEvent e) {
        e.setId(0);
        eventLogPut(e);
        handle(e);
    }

    void enQ(ExEvent e) {
        Check.invariant (e.getId() >= 0, "Id not set");
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
            getExEventQueue(e.getEx().getId()).add(new ProcessValueEvent(v));
            return;
        }
        if (e instanceof PropagateValueEvent) {
            getExEventQueue(e.getEx().getId()).add(0, e);
            return;
        }
        if (e instanceof ProvideExecutionEvent) {
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
        while (! stop.get()) {
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

    _Ex getExecution(_Ex caller, _F targetFunc) {
        _Ex result = null;
        try {
            result = exRecovered.get(new ExFMatcher(caller, targetFunc));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result != null) {
            return result;
        } else {
            return createExecution(targetFunc, caller);
        }
    }

    private _Ex createExecution(_F targetFunc, _Ex returnTo) {
        _Ex result = targetFunc.createExecution(this, returnTo);
        result.setId(IdType.EX.next());
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
        return getExecution(top, targetFunc);
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
        stop.set(false);
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

    @Override
    public String toString() {
        Collection<_Ex> x = new TreeMap<>(idToX).values();
        return "{\"Node\":{" +
                "\"events\":" + events[0] +
                ", \"executions\":" + x +
                "}}";
    }
}
