package micro.event;

public interface EventProcessor {

    void processEvents(EventDriven e);

    void stopProcessingEvents(EventDriven e);

}
