package miso;

import miso.ingredients.Address;
import miso.message.Message;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static miso.ingredients.DNS.dns;

public abstract class Actress implements Runnable {
    private AtomicBoolean terminated = new AtomicBoolean(false);
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

    public void terminate() {
        terminated.compareAndSet(false, true);
    }

    @Override
    public void run() {
        while (!terminated.get())
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

    private void debug(String s) {
        //System.out.println(s);
    }

}
