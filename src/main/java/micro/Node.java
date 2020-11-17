package micro;

import micro.event.*;
import micro.trace.Tracer;
import nano.ingredients.tuples.Tuple;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Node implements Closeable, Hydrator, EventProcessor {
    private final Tracer tracer = new Tracer(false);
    private final EventLogWriter eventLog;

    private final _Ex top; // the ultimate begin of every chain of executions

    private long maxFId = ExTop.TOP_ID;
    private final AtomicLong maxExId = new AtomicLong(ExTop.TOP_ID);
    private final AtomicLong maxEventSequenceNr = new AtomicLong(0);

    private final AtomicInteger delay = new AtomicInteger(0);
    private final AtomicBoolean stop = new AtomicBoolean(true);

    private final Map<Long, _F> idToF = new HashMap<>();
    private final Map<Long, _Ex> idToX = new HashMap<>(); //todo works only single threaded now
    private final Map<Long, _Ex> exRecovered = new HashMap<>();
    private final ConcurrentLinkedQueue<EventDriven> eventDriven = new ConcurrentLinkedQueue<>();

    private final Address address;
    private boolean isRecovery;

    public Node(Address address, boolean clearEventLog) {
        this.address = address;
        eventLog = new EventLogWriter(getEventLogFileName(address), clearEventLog);
        top = createTop();
    }

    private String getEventLogFileName(Address address) {
        return "/home/ssmertnig/temp/" + address.getNode() + ".kryo";
    }

    private void recover(Address address) {
        isRecovery = true;
        long maxOId = 0;
        for (Hydratable h : new EventLogReader(getEventLogFileName(address))) {
            KryoSerializedClass k = KryoSerializedClass.of(h.getClass());
            switch (k) {
                case InitialExecutionCreatedEvent:
                    InitialExecutionCreatedEvent e = (InitialExecutionCreatedEvent) h;

                    _Ex ex = getFForId(e.getFId()).createExecution(e.getExId(), top);
                    maxOId = Math.max(maxOId, e.getExId());
                    exRecovered.put(e.getExId(), ex);
                    idToX.put(e.getExId(), ex);
                    break;

                case ValueReceivedEvent:
                    h.hydrate(this);
                    ValueReceivedEvent r = (ValueReceivedEvent) h;
                    break;

                case ValueProcessedEvent:
                    h.hydrate(this);
                    ValueProcessedEvent v = (ValueProcessedEvent) h;
                    Check.invariant(v.ex instanceof Ex, "no ex");
                    v.ex.recover(v);
                    break;

                case EndOfSequenceEvent:
                    break;

                default:
                    throw new IllegalStateException("invalid case");
            }
        }
        maxExId.set(maxOId);
        isRecovery = false;
    }

    void note(ExEvent e) {
        log(e);
    }

    public void debugValueReceived(ValueReceivedEvent v) {
        String to = null;
        String from = null;
        try {
            _Ex sender = v.value.getSender();
            from = sender == null ? "null" : (v.value.getSender().toString() + " " + v.value.getSender().getId());
            to = (v.getEx() instanceof Ex ? ((Ex) v.getEx()).getLabel() : "") + " " + v.ex.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        log(from + " -> " + to + ": " + v.value.getName() + " " + v.value.get().toString());
    }

    private void processEvents(ConcurrentLinkedQueue<EventDriven> sources) {

        while (true) {
            if (stop.get()) {
                Concurrent.sleep(200);
                continue;
            }
            Concurrent.sleep(delay.get());
            if (sources.size() > 0) {
                EventDriven source = sources.poll();
                while (source.hasNextEvent())
                    source.processNextEvent();
                sources.add(source);
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

    private void log(Event e) {
        eventLog.put(e);
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
        return ++maxFId;
    }

    public long getNextExId() {
        return maxExId.incrementAndGet();
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
        new Thread(() -> processEvents(eventDriven)).start();
    }

    void recover() {
        recover(address);
    }

    public Tuple<Long, Long> reserveIds(int count) {
        long n = maxExId.addAndGet(count);
        return new Tuple<>(n - (count - 1), n);
    }

    public Long getNextEventSequenceNr() {
        return maxEventSequenceNr.incrementAndGet();
    }

    public boolean isRecovery() {
        return isRecovery;
    }

    public _Ex createExecution(F f){
        return createExecution(f, top);
    }

    public _Ex createExecution(F f, _Ex returnTo) {
        InitialExecutionCreatedEvent e = new InitialExecutionCreatedEvent(f.createExecution(maxExId.addAndGet(1), returnTo));
        eventLog.put(e);
        return e.ex;
    }

    @Override
    public synchronized void processEvents(EventDriven e) {
        eventDriven.add(e);
    }

    @Override
    public synchronized void stopProcessingEvents(EventDriven e) {
        eventDriven.remove(e);
    }

    public void registerEx(Ex ex) {
        idToX.put(ex.getId(), ex);
    }
}
