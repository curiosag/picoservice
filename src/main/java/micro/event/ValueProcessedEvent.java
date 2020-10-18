package micro.event;

import micro.Ex;
import micro.Value;

public class ValueProcessedEvent extends ValueEvent {
    ValueProcessedEvent(){
        super();
    }

    public ValueProcessedEvent(Ex ex, Value value) {
        super( ex, value);
    }

    @Override
    public String toString() {
        return "{\"ValueProcessedEvent\":{" +
                "\"value\":" + value +
                ", \"ex\":" + ex.getId() +
                "}}";
    }
}
