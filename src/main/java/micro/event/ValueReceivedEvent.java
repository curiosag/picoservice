package micro.event;

import micro.Value;
import micro._Ex;

public class ValueReceivedEvent extends ValueEvent {

    public ValueReceivedEvent(){
        super();
    }

    public ValueReceivedEvent(long exId, _Ex ex, Value value) {
        super(exId, ex, value);
    }

    @Override
    public String toString() {
        return "{\"ValueReceivedEvent\":{" +
                "\"value\":" + value +
                ", \"ex\":" + ex.getId() +
                "}}";
    }
}
