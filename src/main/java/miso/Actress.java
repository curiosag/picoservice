package miso;

import miso.ingredients.Address;
import miso.message.Message;

import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static miso.ingredients.DNS.dns;

public abstract class Actress implements Runnable {

    public final Address address;

    private Queue<Message> inBox = new ConcurrentLinkedQueue<>();

    public Actress() {
        address = new Address(this.getClass().getSimpleName() + "-" + UUID.randomUUID().toString());
        dns().add(this);
    }

    public Actress(Address address) {
        this.address = address;
        dns().add(this);
    }

    public void recieve(Message message) {
        System.out.println(this.getClass().getSimpleName() + " <-" + message.toString());
        inBox.add(message);
    }

    protected abstract void process(Message message);

    @Override
    public void run() {
        while (true)
            try {
                Message message = inBox.poll();
                if (message != null) {
                    System.out.println(this.getClass().getSimpleName() + " -> " + message.toString());
                    System.out.flush();
                    process(message);
                } else {
                    System.out.println(this.getClass().getSimpleName() + " /");
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        //
                    }
                }

            } catch (Exception e) {
                System.out.println(this.getClass().getSimpleName() + " " + e.toString());
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

}
