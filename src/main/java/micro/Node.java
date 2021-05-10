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

public class Node implements Closeable, Hydrator, EventLoop {
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
    private final ConcurrentLinkedQueue<Crank> cranks = new ConcurrentLinkedQueue<>();

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
        boolean isRecovery = true;
        try {
            long maxOId = 0;
            long maxEventId = 0;
            for (Hydratable h : new EventLogReader(getEventLogFileName(address))) {
                KryoSerializedClass k = KryoSerializedClass.of(h.getClass());
                switch (k) {
                    case InitialExecutionCreatedEvent:
                        ExecutionCreatedEvent e = (ExecutionCreatedEvent) h;

                        _Ex ex = getFForId(e.getFId()).createExecution(e.getExId(), top);
                        maxOId = Math.max(maxOId, e.getExId());
                        exRecovered.put(e.getExId(), ex);
                        idToX.put(e.getExId(), ex);
                        break;

                    case IdsAllocatedEvent, ValueEnqueuedEvent, ValueProcessedEvent:
                        h.hydrate(this);
                        ExEvent x = (ExEvent) h;
                        Check.invariant(x.ex instanceof Ex, "no ex");
                        x.ex.recover(x);
                        if(k == KryoSerializedClass.IdsAllocatedEvent){
                            Long to = ((IdsAllocatedEvent) x).rangeTo;
                            maxEventId = Math.max(maxEventId, to);
                        }
                        break;

                    case ExDoneEvent:
                        break;

                    default:
                        throw new IllegalStateException("invalid case");
                }
            }
            maxExId.set(maxOId);
            maxEventSequenceNr.set(maxEventId);
        } finally {
            isRecovery = false;
        }
    }

    void note(ExEvent e) {
        if (isRecovery) {
            return;
        }
        log(e);
        if (e instanceof ExDoneEvent) {
            synchronized (this) {
                cranks.remove(e.ex);
            }
        }
    }

    public void debugValueEnqueuedEvent(ValueEnqueuedEvent v) {
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

    @Override
    public void loop(){
        loop(cranks);
    }

    private void loop(ConcurrentLinkedQueue<Crank> sources) {

        while (true) {
            if (stop.get()) {
                Concurrent.sleep(200);
                continue;
            }
            Concurrent.sleep(delay.get());

            Crank source = sources.poll();
            if (source != null) {
                while (source.isMoreToDo()) {
                    source.crank();
                }
                sources.add(source);
            } else {
                Concurrent.await(500);
            }
        }
    }

    @Override
    public void close() {
        tracer.close();
        eventLog.close();
    }

    private void log(Event e) {
        if(e instanceof ValueEnqueuedEvent)
        {
            debugValueEnqueuedEvent((ValueEnqueuedEvent)e);
        }
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
        new Thread(this::loop).start();
    }

    void recover() {
        recover(address);
    }

    public Tuple<Long, Long> allocateIds(int count) {
        long n = maxEventSequenceNr.addAndGet(count);
        return new Tuple<>(n - (count - 1), n);
    }

    public _Ex createExecution(F f) {
        return createExecution(f, top);
    }

    public _Ex createExecution(F f, _Ex returnTo) {
        Ex ex = f.createExecution(maxExId.addAndGet(1), returnTo);
        eventLog.put(new ExecutionCreatedEvent(ex));
        return ex;
    }

    @Override
    public synchronized void register(_Ex e) {
        idToX.put(((Ex) e).getId(), (Ex) e);
        cranks.add(e);
    }

}
