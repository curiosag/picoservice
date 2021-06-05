package micro.event;

import micro.Ex;
import micro.Value;

public class ValueReceivedEvent extends ValueEvent {

    public ValueReceivedEvent(){
        super();
    }

    public ValueReceivedEvent(Ex ex, Value value) {
        super(ex, value);
    }
}
