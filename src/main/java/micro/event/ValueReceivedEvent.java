package micro.event;

import micro.Value;
import micro._Ex;

public class ValueReceivedEvent extends ValueEvent {

    public ValueReceivedEvent() {
        super();
    }

    public ValueReceivedEvent(_Ex ex, Value value) {
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
