package micro;

import micro.event.Crank;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Cranks {

    private ConcurrentHashMap<Long, Crank> seen = new ConcurrentHashMap<>();

    private final List<ConcurrentLinkedQueue<Crank>> qs = new ArrayList<>();
    private final int executors;
    private final List<Long> tids = new ArrayList<>();
    private final AtomicBoolean stop = new AtomicBoolean();
    private volatile AtomicInteger submitCount = new AtomicInteger();
    private volatile AtomicInteger doleOutCount = new AtomicInteger();
    public volatile int exCount = 0;

    private volatile int idleCount = 0;

    Timer timer = new Timer();

    public Cranks(int executors) {
        this.executors = executors;
        for (int i = 0; i < executors; i++) {
            qs.add(new ConcurrentLinkedQueue<>());
        }

        if (executors > 1)
            rewatch();
    }

    private void rewatch() {
        if (!stop.get())
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    idleWatch();
                }
            }, 100L);
    }

    private void idleWatch() {
        if (getCrankCount() == 0) {
            idleCount = idleCount + 1;
        }
        if (idleCount == 2) {
            List<Crank> leftOver = seen.values().stream()
                    .filter(i -> !((Ex) i).inBox.isEmpty())
                    .collect(Collectors.toList());
            leftOver.forEach(this::schedule);
            if (leftOver.size() > 0)
                System.out.printf("in:%d out:%d stuck:%d out of %d\n", submitCount.get(), doleOutCount.get(), leftOver.size(), seen.size());
            idleCount = 0;
        }
        rewatch();
    }

    public void stop() {
        stop.set(true);
    }

    public Crank poll() {
        Crank result = getQueue().poll();
        if (result != null) {
            doleOutCount.incrementAndGet();
        }
        return result;
    }

    public void schedule(Crank c) {
        submitCount.incrementAndGet();
        addToPartition(c);
    }

    public int getCrankCount() {
        return qs.stream().map(ConcurrentLinkedQueue::size).reduce(0, Integer::sum);
    }

    private ConcurrentLinkedQueue<Crank> getQueue() {
        long tid = Thread.currentThread().getId();
        int idx = tids.indexOf(tid);
        if (idx < 0) {
            synchronized (tids) {
                tids.add(tid);
            }
            idx = tids.size() - 1;
        }

        return qs.get(idx);
    }

    private void addToPartition(Crank current) {
        exCount++;
        seen.put(current.getId(), current);
        qs.get(Math.toIntExact(current.getId() % executors)).add(current);
    }

    public void done(Crank current) {
        seen.remove(current.getId());
    }
}