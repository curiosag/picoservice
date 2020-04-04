package micro.event;

import micro.Value;
import micro._Ex;

public class ValueProcessedEvent extends ValueEvent {
    public ValueProcessedEvent(){
        super();
    }

    public ValueProcessedEvent( _Ex ex, Value value) {
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
