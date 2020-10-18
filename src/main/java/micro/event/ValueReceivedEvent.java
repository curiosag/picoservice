package micro.event;

import micro.Ex;
import micro.Value;

public class ValueReceivedEvent extends ValueEvent {

    ValueReceivedEvent() {
        super();
    }

    public ValueReceivedEvent(Ex ex, Value value) {
        super(ex, value);
    }

    @Override
    public String toString() {
        return "{\"ValueReceivedEvent\":{" +
                "\"value\":" + value +
                ", \"ex\":" + ex.getId() +
                "}}";
    }
}
