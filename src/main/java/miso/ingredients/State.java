package miso.ingredients;

import java.time.LocalDateTime;

public class State {
    final Source source;
    LocalDateTime lastRequested;
    Long millisTimeout = 0L;

    public State(Source source) {
        this.source = source;
    }
}
