package miso;

import miso.ingredients.Address;
import miso.message.Message;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static miso.ingredients.DNS.dns;

public abstract class Actress implements Runnable {

    public final Address address;

    private Queue<Message> current = new ConcurrentLinkedQueue<>();

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
        current.add(message);
    }

    public void send(Actress recipient, Message message) {
        recipient.recieve(message);
    }

    public void send(Address recipient, Message message) {
        dns().resolve(recipient).recieve(message);
    }

    protected abstract void process(Message message);

    @Override
    public void run() {
        while (true)
            try {
                Message message = current.poll();
                if (message != null) {
                    System.out.println(this.getClass().getSimpleName() + " -> " + message.toString());
                    System.out.flush();
                    process(message);
                } else {
                    System.out.println(this.getClass().getSimpleName() + " /");
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }

            } catch (Exception e) {
                System.out.println(this.getClass().getSimpleName() + " " + e.toString());
                return;
            }
    }
}
