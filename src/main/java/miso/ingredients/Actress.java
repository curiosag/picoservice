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

    public static void start(Actress a) {
        new Thread(a).start();
        cast.add(a);
    }

    public static void shutdown() {
        cast.forEach(Actress::stop);
        await(() -> cast.stream().allMatch(Actress::isStopped));
        cast.clear();
    }

    private AtomicBoolean stopping = new AtomicBoolean(false);

    public boolean isStopped() {
        return stopped;
    }

    private boolean stopped;

    private static int maxAddress = 0;

    public final Address address;

    private Queue<Message> inBox = new ConcurrentLinkedQueue<>();

    public Actress() {
        address = new Address(this.getClass().getSimpleName() + "-" + maxAddress++);
        dns().add(this);
    }

    public void recieve(Message message) {
        debug(this.getClass().getSimpleName() + " <- " + message.source.host.getClass().getSimpleName() + " " + message.toString());
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
                    debug(this.getClass().getSimpleName() + " ** " + message.toString());
                    process(message);
                } else {
                    Thread.yield();
                }

            } catch (Exception e) {
                debug(this.getClass().getSimpleName() + " " + e.toString());
                return;
            }
        stopped = true;
        onStopped();
    }

    abstract void onStopped();

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

    protected void debug(String s) {
        // System.out.println(s);
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
