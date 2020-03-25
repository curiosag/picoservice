package micro.event;

public abstract class NodeEvent extends Event{
    public NodeEvent(long id) {
        super(id);
    }

    protected NodeEvent() {
        super();
    }
}
