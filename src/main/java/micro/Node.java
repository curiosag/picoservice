package micro;

import micro.event.*;
import micro.event.eventlog.EventLogReader;
import micro.event.eventlog.EventLogWriter;
import micro.event.eventlog.NullEventLogWriter;
import micro.event.eventlog.kryoeventlog.KryoEventLogReader;
import micro.event.eventlog.kryoeventlog.KryoEventLogWriter;
import micro.event.serialization.SerioulizedEvent;
import micro.gateway.Gateway;
import micro.trace.Tracer;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static micro.ExTop.TOP_ID;

public class Node implements Env, Closeable {
    private final Address address;

    private final Tracer tracer = new Tracer(false);
    private final EventLogWriter logWriter;
    private EventLogReader logReader;

    private final AtomicInteger delay = new AtomicInteger(0);
    private final AtomicBoolean stop = new AtomicBoolean(true);

    private final Map<Long, _F> idToF = new HashMap<>();
    private final Map<Long, _Ex> idToEx = new TreeMap<>(); //todo works only single threaded now
    private final ConcurrentLinkedQueue<Crank> cranks = new ConcurrentLinkedQueue<>();

    private long maxFId = TOP_ID; // TODO AtomicLong at least for maxFId, perhaps compiler-assigned FIds?
    private final AtomicLong maxExId = new AtomicLong(TOP_ID);
    private final _Ex top = initializeTop(); // the ultimate begin of every tree of executions

    private int maxExCount = 0;
    private boolean recover = false;
    private final boolean useEventLog;
    private static final boolean debug = true;

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

    private void removeEx(Long exId) {
        idToEx.remove(exId);
    }

    private _Ex getReturnTarget(ExCreatedEvent e) {
        return getExForId(e.exReturnToId);
    }

    @Override
    public void note(ExEvent e) {
        if (recover) {
            return;
        }
        log(e);
    }

    public void run(boolean recover) {
        if (recover) {
            this.recover = recover;
            recover(address);
            debug("*** RECOVERED ***");
        }
        crankRoundRobin(cranks);
    }

    @SuppressWarnings("ConditionalBreakInInfiniteLoop")
    private void crankRoundRobin(Queue<Crank> cranks) {
        debug("*** RUN ***");
        while (true) {
            if (stop.get()) {
                break;
            }
            Concurrent.sleep(delay.get());

            maxExCount = Math.max(maxExCount, cranks.size());
            Crank current = cranks.poll();
            if (current != null) {
                while (current.isMoreToDoRightNow()) {
                    current.crank();
                }
                if (current.isDone()) {
                    idToEx.remove(current.getId());
                } else {
                    cranks.add(current);
                }
            } else {
                Concurrent.await(200);
            }
        }
    }

    private void recover(Address address) {
        Check.preCondition(recover);

        if (logReader == null)
            logReader = new KryoEventLogReader(getEventLogFileName(address));

        long maxExId = 0;

        for (Hydratable h : logReader) {
            switch (SerioulizedEvent.of(h.getClass())) {
                case ExCreatedEvent, DependendExCreatedEvent -> {
                    ExCreatedEvent e = (ExCreatedEvent) h;
                    _F f = getFForId(e.getFId());
                    _Ex ex = f.createExecution(e.exId, getReturnTarget(e));
                    maxExId = Math.max(maxExId, e.exId);
                    idToEx.put(e.exId, ex);
                    if (e instanceof DependendExCreatedEvent d) {
                        d.ex = ex;
                        idToEx.get(d.dependingOnId).recover(e);
                    }
                }
                case AfterlifeEventCanPropagatePendingValues, ValueReceivedEvent, PropagationTargetExsCreatedEvent, ValueEnqueuedEvent, ValueProcessedEvent -> {
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

    @Override
    public void close() {
        stop();
        tracer.close();
        try {
            logWriter.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void log(Event e) {
        if (e instanceof ValueEnqueuedEvent)
            debugValueEnqueuedEvent((ValueEnqueuedEvent) e);
        if (e instanceof ExCreatedEvent) {
            _Ex ex = ((ExCreatedEvent) e).ex;
            cranks.add(ex);
            idToEx.put(ex.getId(), ex);
        }
        logWriter.put(e);
    }

    @Override
    public Address getAddress() {
        return address;
    }

    void log(String msg) {
        System.out.println(Thread.currentThread().getId() + " " + msg);
    }

    public void debug(String s) {
        if (debug)
            log(s);
    }


    @Override
    public _Ex getTop() {
        return top;
    }

    @Override
    public long getNextFId() {
        return ++maxFId;
    }

    @Override
    public long getNextExId() {
        return maxExId.incrementAndGet();
    }

    @Override
    public void addF(_F f) {
        idToF.put(f.getId(), f);
    }

    @Override
    public void setDelay(@SuppressWarnings("SameParameterValue") int delay) {
        this.delay.set(delay);
    }

    @Override
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
        idToF.put(TOP_ID, result.getTemplate());
        idToEx.put(TOP_ID, result);
        return result;
    }

    @Override
    public void start() {
        start(false);
    }

    @Override
    public void start(boolean recover) {
        this.stop.set(false);
        new Thread(() -> run(recover)).start();
    }

    @Override
    public List<_Ex> createTargets(_Ex source, List<_F> targetTemplates) {
        return targetTemplates.stream().map(t -> createExecution(t, source)).collect(Collectors.toList());
    }

    @Override
    public _Ex createExecution(F f) {
        return createExecution(f, top);
    }

    @Override
    public _Ex createExecution(_F f, _Ex returnTo) {
        Check.preCondition(!recover);
        if (f.equals(returnTo.getTemplate())) {
            return returnTo;
        }
        if (f.equals(ExTop.instance.getTemplate())) {
            return ExTop.instance;
        }
        _Ex ex = createOrRecycleExecution(f, returnTo);
        return ex;
    }

    @Override
    public DependendExCreatedEvent createDependentExecutionEvent(_F f, _Ex returnTo, _Ex dependingOn) {
        Check.preCondition(!recover);
        _Ex ex = createOrRecycleExecution(f, returnTo);
        return new DependendExCreatedEvent((Ex) ex, (Ex) dependingOn);
    }

    private _Ex createOrRecycleExecution(_F f, _Ex returnTo) {
        if (f.isTailRecursive()) {
            Check.invariant(f.getAddress() == Address.localhost);
            Optional<_Ex> ex = getRecursiveF(f, returnTo);
            if (ex.isPresent())
                return ex.get();
        }

        _Ex result = f.createExecution(maxExId.addAndGet(1), returnTo);
        log(new ExCreatedEvent((Ex) result));
        return result;
    }

    private Optional<_Ex> getRecursiveF(_F f, _Ex current) {
        if (current instanceof Gateway || current instanceof ExTop)
            return Optional.empty();

        if (current.getTemplate().equals(f))
            return Optional.of(current);
        else
            return getRecursiveF(f, current.returnTo());
    }

    @Override
    public void relatchExecution(_F f, _Ex returnTo) {
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

    @Override
    public int getMaxExCount() {
        return maxExCount;
    }

    public int getCrankCount() {
        return cranks.size();
    }
}
