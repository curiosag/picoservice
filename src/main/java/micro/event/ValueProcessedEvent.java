package micro.event;

import micro.Ex;
import micro.Value;

public class ValueProcessedEvent extends ValueEvent {
    public ValueProcessedEvent(){
        super();
    }

    public ValueProcessedEvent(Ex ex, Value value) {
        super(ex, value);
    }
}
