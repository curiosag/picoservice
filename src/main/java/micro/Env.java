package micro;

import micro.actor.Message;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Env implements Runnable {

    private Queue<Message> messages = new ConcurrentLinkedQueue<>();

    private AtomicInteger delay = new AtomicInteger(0);

    public int getDelay() {
        return delay.get();
    }

    public void setDelay(int delay) {
        this.delay.set(delay);
    }

    public Env() {
        new Thread(this).start();
        new Thread(this).start();
        new Thread(this).start();
    }

    public void log(String msg) {
        System.out.println(Thread.currentThread().getId() + " " + msg);
    }

    public void debug(String msg) {
        System.out.println(Thread.currentThread().getId() + " " + msg);
    }

    public void enq(Value v, Ex target) {
        messages.add(new Message(v, target));
    }

    @Override
    public void run() {
        while (true) {
            Thread.yield();
            sleep(delay.get());
            Message m = messages.poll();
            if (m != null) {
                m.target.process(m.value);
            } else {
                sleep(100);
            }
        }
    }


    private void sleep(int millis) {
        if (millis > 0)
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
    }

}
