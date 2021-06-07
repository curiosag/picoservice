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
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<Long, _F> idToF = new ConcurrentHashMap<>();
    private final Map<Long, _Ex> idToEx = new ConcurrentHashMap<>(); //todo works only single threaded now
    private final Cranks cranks = new Cranks();

    private long maxFId = TOP_ID; // TODO AtomicLong at least for maxFId, perhaps compiler-assigned FIds?
    private final AtomicLong maxExId = new AtomicLong(TOP_ID);

    private final _Ex top = initializeTop(); // the ultimate begin of every tree of executions

    private static final int maxExecutors = 1;
    private int maxExCount = 0;
    private boolean recover = false;
    private final boolean useEventLog;
    private static final boolean debug = true;
    private AtomicInteger stopped = new AtomicInteger();

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

    public int getThreadsUsed() {
        return maxExecutors;
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
        startExecutors(maxExecutors);
    }

    private void startExecutors(int number) {
        for (int i = 0; i < number; i++) {
            new Thread(() -> crankRoundRobin(cranks)).start();
        }
    }

    @SuppressWarnings("ConditionalBreakInInfiniteLoop")
    private void crankRoundRobin(Cranks cranks) {
        debug("*** RUN ***");
        while (true) {
            if (stop.get()) {
                stopped.incrementAndGet();
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
                Thread.onSpinWait();
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
                case ExCreatedEvent -> {
                    ExCreatedEvent e = (ExCreatedEvent) h;
                    _F f = getFForId(e.getFId());
                    _Ex ex = f.createExecution(e.exId, getReturnTarget(e));
                    maxExId = Math.max(maxExId, e.exId);
                    idToEx.put(e.exId, ex);
                }
                case DependendExCreatedEvent -> {
                    DependendExCreatedEvent e = (DependendExCreatedEvent) h;
                    e.ex = idToEx.get(e.exId);
                    idToEx.get(e.dependingOnId).recover(e);
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
                .forEach(c -> {
                    straightenOutPostRecovery(c);
                    cranks.add(c);
                });

        recover = false;
    }

    private void straightenOutPostRecovery(Crank c) {
        if(c instanceof Ex ex)
            ex.straightenOutPostRecovery();
    }

    @Override
    public void close() {
        stop();
        Concurrent.await(() -> this.stopped.get() == maxExecutors);
        tracer.close();
        try {
            logWriter.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private synchronized void log(Event e) {
        if (e instanceof ValueEnqueuedEvent)
            debugValueEnqueuedEvent((ValueEnqueuedEvent) e);
        if (e instanceof ExCreatedEvent ee) {
            _Ex ex = ee.ex;

            cranks.add(ex);
            idToEx.put(ex.getId(), ex);

        }
        logWriter.put(e);
    }

    @Override
    public Address getAddress() {
        return address;
    }

    private Queue<String> logQueue = new ConcurrentLinkedQueue<>();

    void log(String msg) {
        logQueue.add(msg);
    }

    private void startLogWriter() {
        new Thread(() -> {
            while (stopped.get() != maxExecutors) {
                String current = logQueue.poll();
                if (current != null)
                    System.out.println(current);
                else {
                    Thread.onSpinWait();
                }
            }
        }).start();
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
        startLogWriter();
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

        _Ex result = f.createExecution(getNextExId(), returnTo);
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
