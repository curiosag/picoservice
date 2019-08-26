package miso.ingredients;

public class State {
    final Source source;
    Long lastRequested;
    Long millisTimeout = 0L;

    public State(Source source) {
        this.source = source;
    }
}
