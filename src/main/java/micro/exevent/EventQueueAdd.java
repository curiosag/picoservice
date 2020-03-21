package micro.exevent;

public class EventQueueAdd implements EnvEvent {
    public final ExEvent e;

    public EventQueueAdd(ExEvent e) {
        this.e = e;
    }
}
