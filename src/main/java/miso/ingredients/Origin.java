package miso.ingredients;

import java.util.Objects;

import static miso.ingredients.Nop.nop;

public class Origin {
    public final Function<?> sender;
    public final Function<?> scope;
    public final Long executionId;
    public final Integer callLevel;

    private Origin(Function<?> sender, Function<?> scope, Long executionId, Integer callLevel) {
        this.sender = sender;
        this.scope = scope;
        this.executionId = executionId;
        this.callLevel = callLevel;
    }

    public static Origin origin(Function<?> sender, Function<?> scope, Long executionId, Integer callLevel) {
        return new Origin(sender, scope, executionId, callLevel);
    }

    Origin sender(Function<?> sender) {
        return origin(sender, scope, executionId, callLevel);
    }

    @Override
    public String toString() {
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
