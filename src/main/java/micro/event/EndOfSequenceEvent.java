package micro.event;

import micro.Ex;

public class EndOfSequenceEvent extends ExEvent {
    public EndOfSequenceEvent(Ex ex) {
        super(ex);
    }

    public EndOfSequenceEvent() {

    }
}
