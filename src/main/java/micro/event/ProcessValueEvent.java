package micro.event;

public class ProcessValueEvent extends ValueEvent {

    public ProcessValueEvent(){
        super();
    }

    public ProcessValueEvent(ValueReceivedEvent trigger) {
        super( trigger.ex, trigger.value);
    }

    @Override
    public String toString() {
        return "{\"ProcessValueEvent\":{" +
                "\"value\":" + value +
                ", \"ex\":" + ex.getId() +
                "}}";
    }
}
