package micro;

import micro.event.*;
import micro.event.eventlog.EventLogReader;
import micro.event.eventlog.EventLogWriter;
import micro.event.eventlog.NullEventLogWriter;
import micro.event.eventlog.kryoeventlog.KryoEventLogReader;
import micro.event.eventlog.kryoeventlog.KryoEventLogWriter;
import micro.event.serialization.SerioulizedEvent;
import micro.trace.Tracer;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Node implements Closeable, Hydrator {
    private final Address address;

    private final Tracer tracer = new Tracer(false);
    private final EventLogWriter logWriter;
    private EventLogReader logReader;

    private final AtomicInteger delay = new AtomicInteger(0);
    private final AtomicBoolean stop = new AtomicBoolean(true);

    private final Map<Long, _F> idToF = new HashMap<>();
    private final Map<Long, _Ex> idToEx = new TreeMap<>(); //todo works only single threaded now
    private final ConcurrentLinkedQueue<Crank> cranks = new ConcurrentLinkedQueue<>();

    private long maxFId = ExTop.TOP_ID; // TODO AtomicLong at least for maxFId, perhaps compiler-assigned FIds?
    private final AtomicLong maxExId = new AtomicLong(ExTop.TOP_ID);
    private final _Ex top = initializeTop(); // the ultimate begin of every tree of executions

    private boolean recover;
    private final boolean useEventLog;
    private static final boolean debug = false;

    Node(Address address, EventLogReader logReader, EventLogWriter logWriter) {
        this.address = address;
        this.logWriter = logWriter;
        this.logReader = logReader;
        this.useEventLog = false;
    }

    public Node(Address address, boolean useEventLog, boolean clearEventLog) {
        this.address = address;
        logWriter = useEventLog ? new KryoEventLogWriter(getEventLogFileName(address), clearEventLog) : NullEventLogWriter.instance;
        this.useEventLog = useEventLog;
    }

    private String getEventLogFileName(Address address) {
        return "/home/ssmertnig/temp/" + address.getNode() + ".kryo";
    }

    private void recover(Address address) {
        Check.preCondition(recover);

        if (logReader == null)
            logReader = new KryoEventLogReader(getEventLogFileName(address));

        long maxExId = 0;

        for (Hydratable h : logReader) {
            switch (SerioulizedEvent.of(h.getClass())) {
                case ExCreatedEvent -> {
                    ExCreatedEvent e = (ExCreatedEvent) h;
                    _F f = getFForId(e.getFId());
                    _Ex ex = f.createExecution(e.exId, getReturnTarget(e, f));
                    maxExId = Math.max(maxExId, e.exId);
                    idToEx.put(e.exId, ex);
                }
                case ValueReceivedEvent, PropagationTargetExsCreatedEvent, ValueEnqueuedEvent, ValueProcessedEvent -> {
                    h.hydrate(this);
                    ExEvent e = (ExEvent) h;
                    e.ex.recover(e);
                }
                case ExDoneEvent -> removeEx(((ExEvent) h).exId);
                default -> throw new IllegalStateException("invalid type of SerioulizedEvent: " + h.getClass().getSimpleName());
            }
        }

        this.maxExId.set(maxExId);
        idToEx.values().stream()
                .filter(i -> !i.equals(top))
                .forEach(cranks::add);
        recover = false;
    }

    private void removeEx(Long exId){
        idToEx.remove(exId);
    }

    private _Ex getReturnTarget(ExCreatedEvent e, _F f) {
        Optional<Relatch> re = getRelatch(e.exId);
        if (re.isPresent()) {
            Check.invariant(re.get().f.equals(f));
            return re.get().returnTo;
        }
        return getExForId(e.exReturnToId);
    }

    void note(ExEvent e) {
        if (recover) {
            return;
        }
        log(e);
    }

    public void run(boolean recover) {
        if (recover) {
            this.recover = recover;
            recover(address);
        }
        crankRoundRobin(cranks);
    }

    private void crankRoundRobin(ConcurrentLinkedQueue<Crank> cranks) {
        while (true) {
            if (stop.get()) { //TODO its rather a non-working suspend. stop should terminate
                Concurrent.sleep(1000);
                continue;
            }
            Concurrent.sleep(delay.get());

            // removal of cranks that are done gets triggered by ExDoneEvent
            Crank source = cranks.poll();
            if (source != null) {
                while (source.isMoreToDo()) {
                    source.crank();
                }
                cranks.add(source);
            } else {
                Concurrent.await(500);
            }
        }
    }

    @Override
    public void close() {
        tracer.close();
        try {
            logWriter.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void log(Event e) {
        if (e instanceof ValueEnqueuedEvent) {
            debugValueEnqueuedEvent((ValueEnqueuedEvent) e);
        }
        if (e instanceof ExDoneEvent) {
            synchronized (this) {
                cranks.remove(((ExDoneEvent)e).ex);
            }
        }
        logWriter.put(e);
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
        idToF.put(f.getId(), (F) f); //TODO (F)
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
        return Check.notNull(idToEx.get(exId));
    }

    @Override
    public _F getFForId(long fId) {
        return Check.notNull(idToF.get(fId));
    }

    private _Ex initializeTop() {
        _Ex result = new ExTop(address);
        idToF.put(ExTop.TOP_ID, result.getTemplate());
        idToEx.put(ExTop.TOP_ID, result);
        return result;
    }

    void start() {
        start(false);
    }

    void start(boolean recover) {
        Check.invariant(recover || relatches.size() == 0);
        this.stop.set(false);
        new Thread(() -> run(recover)).start();
    }

    public List<_Ex> allocatePropagationTargets(_Ex source, List<_F> targetTemplates) {
        return targetTemplates.stream().map(t -> createExecution(t, source)).collect(Collectors.toList());
    }

    public _Ex createExecution(F f) {
        return createExecution(f, top);
    }

    public _Ex createExecution(_F f, _Ex returnTo) {
        Check.preCondition(!recover);
        _Ex ex = f.createExecution(maxExId.addAndGet(1), returnTo);
        log(new ExCreatedEvent((Ex) ex));
        cranks.add(ex);
        idToEx.put(ex.getId(), ex);
        return ex;
    }

    private record Relatch(Long exid, _F f, _Ex returnTo) {
    }

    List<Relatch> relatches = new ArrayList<>();

    private Optional<Relatch> getRelatch(long exid) {
        Optional<Relatch> result = relatches.stream().filter(i -> i.exid == exid).findAny();
        result.ifPresent(relatch -> relatches.remove(relatch));
        return result;
    }

    public void relatchExecution(long exId, _F f, _Ex returnTo) {
        //relatches.add(new Relatch(exId, f, returnTo));
        idToEx.put(returnTo.getId(), returnTo);
    }

    public void debugValueEnqueuedEvent(ValueEnqueuedEvent v) {
        if (!debug) {
            return;
        }
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

}
