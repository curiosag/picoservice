package micro;

import micro.event.Crank;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Cranks {

    private final ConcurrentLinkedQueue<Crank> cranks = new ConcurrentLinkedQueue<>();

    public Queue getQueue(){
        return cranks;
    }

    public Crank poll() {
        return cranks.poll();
    }

    public void add(Crank c) {
        if (contains(c))
            throw new IllegalStateException();
        cranks.add(c);
    }

    public boolean contains(Crank c) {
        return cranks.contains(c);
    }

    public int size() {
        return cranks.size();
    }

}