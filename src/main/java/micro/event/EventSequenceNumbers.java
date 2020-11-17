package micro.event;

import micro.Check;

import java.util.function.Supplier;

public class EventSequenceNumbers implements EventSequenceNumberProvider{

    private long eventSeqNr = 0;
    private int eventSeqElementNr = 0;
    private final Supplier<Long> getNextEventSequenceNr;

    public EventSequenceNumbers(Supplier<Long> getNextEventSequenceNr) {
        this.getNextEventSequenceNr = getNextEventSequenceNr;
    }

    public void startEventSequence() {
        Check.invariant(eventSeqNr + eventSeqElementNr == 0);
        eventSeqNr = getNextEventSequenceNr.get();
        eventSeqElementNr = 1;
    }

    public void finishEventSequence() {
        eventSeqNr = -1;
        eventSeqElementNr = -1;
    }

    public long getEventSequenceNr() {
        Check.invariant(eventSeqNr >= 0);
        return eventSeqNr;
    }

    public int getNextEventSequenceElementNr(){
        Check.invariant(eventSeqNr >= 0);
        return ++eventSeqElementNr;
    }

}
