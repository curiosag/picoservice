package miso.ingredients;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static miso.ingredients.DNS.dns;

public abstract class Actress implements Runnable {

    private static final List<Actress> cast = new ArrayList<>();

    static public boolean debug = false;

    public static void start(Actress a) {
        new Thread(a).start();
        cast.add(a);
    }

    public static void cleanupFunctions(Long runId) {
        cast.forEach(c -> {
            if (c instanceof Function) {
                ((Function) c).cleanup(runId);
            }
        });
    }

    public void checkSanityOnStop() {

    }

    ;

    public static void debug() {
        debug = true;
    }

    public static void noDebug() {
        debug = true;
    }

    public static void shutdown() {
        cast.forEach(Actress::stop);
        await(() -> cast.stream().allMatch(Actress::isStopped));
        cast.forEach(Actress::checkSanityOnStop);
        cast.clear();
        maxAddress = 0;
    }

    private AtomicBoolean stopping = new AtomicBoolean(false);

    public boolean isStopped() {
        return stopped;
    }

    private boolean stopped;

    private static int maxAddress = 0;

    public final Address address;

    Queue<Message> inBox = new ConcurrentLinkedQueue<>();

    public Actress() {
        address = new Address(this.getClass().getSimpleName() + "-" + maxAddress++);
        dns().add(this);
    }

    public void label(String sticker) {
        address.setSticker(sticker);
    }

    public void receive(Message message) {
        debug(this.address.toString() + " <- " + message.origin.sender.address.toString() + " " + message.toString());
        inBox.add(message);
    }

    protected abstract void process(Message message);

    public void stop() {
        stopping.compareAndSet(false, true);
    }

    @Override
    public void run() {
        stopped = false;
        stopping.set(false);

        while (!stopping.get())
            try {
                Message message = inBox.poll();
                if (message != null) {
                    debug(this.address + " !! " + message.toString());
                    process(message);
                } else {
                    Thread.yield();
                }

            } catch (Exception e) {
                debug(this.address + " " + e.toString());
                throw e;
            }
        stopped = true;
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

    protected static void debug(String s) {
        if (debug) {
            System.out.println(s);
        }
    }

    private static void await(Supplier<Boolean> condition) {
        while (!condition.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //
            }
        }
    }
}
