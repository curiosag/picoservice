package miso.ingredients;

import java.util.Objects;

public class Origin {
    public final Function<?> sender;
    public final Function<?> scope;
    public final Long executionId;
    public final Integer callLevel;
    public final Long seqNr;
    public boolean partiallyApplied;

    private Origin(Function<?> sender, Function<?> scope, Long executionId, Integer callLevel, Long seqNr) {
        this.sender = sender;
        this.scope = scope;
        this.executionId = executionId;
        this.callLevel = callLevel;
        this.seqNr = seqNr;
    }

    public static Origin origin(Function<?> sender, Function<?> scope, Long executionId, Integer callLevel, Long seqNr) {
        return new Origin(sender, scope, executionId, callLevel, seqNr);
    }

    Origin sender(Function<?> sender) {
        return origin(sender, scope, executionId, callLevel, seqNr + 1L);
    }

    Origin incSeqNr() {
        return origin(sender, scope, executionId, callLevel, seqNr + 1L);
    }

    @Override
    public String toString() {
        //TODO
        return "";
       // return String.format("Origin:(%d/%d)%s->%s (%d states left)", executionId, callLevel, scope.address, sender.address, sender.executionStates.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Origin origin = (Origin) o;
        return Objects.equals(sender, origin.sender) &&
                Objects.equals(scope, origin.scope) &&
                Objects.equals(executionId, origin.executionId) &&
                Objects.equals(callLevel, origin.callLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, scope, executionId, callLevel);
    }
}
