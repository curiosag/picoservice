package micro.event;

import micro.Value;
import micro._Ex;

public class ValueProcessedEvent extends ValueEvent {
    public ValueProcessedEvent(){
        super();
    }

    public ValueProcessedEvent(long exId, _Ex ex, Value value) {
        super(exId, ex, value);
    }

    @Override
    public String toString() {
        return "{\"ValueProcessedEvent\":{" +
                "\"value\":" + value +
                ", \"ex\":" + ex.getId() +
                "}}";
    }
}
