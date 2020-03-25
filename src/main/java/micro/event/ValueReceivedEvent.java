package micro.event;

import micro.Ex;
import micro.Value;

public class ValueReceivedEvent extends ValueEvent {

    public ValueReceivedEvent(){
        super();
    }

    public ValueReceivedEvent(long exId, Ex ex, Value value) {
        super(exId, ex, value);
    }
}
