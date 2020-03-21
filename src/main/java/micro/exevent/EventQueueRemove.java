package micro.exevent;

public class EventQueueRemove implements EnvEvent {
    public final ExEvent e;

    public EventQueueRemove(ExEvent e) {
        this.e = e;
    }
}
