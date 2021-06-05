package micro.event;

import micro.Ex;
import micro.Value;

public class ValueEnqueuedEvent extends ValueEvent {

    public ValueEnqueuedEvent(){
        super();
    }

    public ValueEnqueuedEvent(Ex ex, Value value) {
        super(ex, value);
    }
}
