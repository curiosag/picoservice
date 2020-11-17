package micro.event;

public interface EventDriven {

    boolean hasNextEvent();

    void processNextEvent();

}
