package micro.event;

public interface EventSequenceNumberProvider {
    long getEventSequenceNr();
    int getNextEventSequenceElementNr();
}
