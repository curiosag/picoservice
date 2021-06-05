package micro.event;

import micro.Ex;

public class InitialExCreatedEvent extends ExCreatedEvent {
    public InitialExCreatedEvent(Ex ex) {
        super(ex);
    }

}
