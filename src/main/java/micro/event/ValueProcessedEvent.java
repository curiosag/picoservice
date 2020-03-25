package micro.event;

import micro.Ex;

public class ValueProcessedEvent extends ExEvent {
    public String valueName;

    ValueProcessedEvent(){
    }

    public ValueProcessedEvent(long eventId, Ex ex, String valueName) {
        super(eventId, ex);
        this.valueName = valueName;
    }
}
