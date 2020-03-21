package micro.exevent;

import micro.Ex;
import micro.Value;

public class ValueReceivedEvent extends ValueEvent {
    public ValueReceivedEvent(Ex ex, Value value) {
        super(ex, value);
    }
}
