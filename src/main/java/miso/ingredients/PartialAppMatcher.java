package miso.ingredients;

import java.util.Objects;

public class PartialAppMatcher {
    private final Function<?> scope;
    final Long executionId;

    private PartialAppMatcher(Origin origin) {
        this.scope = origin.scope;
        this.executionId = origin.executionId;
    }

    static PartialAppMatcher matcher(Origin o){
        return new PartialAppMatcher(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartialAppMatcher that = (PartialAppMatcher) o;
        return Objects.equals(scope, that.scope) &&
                Objects.equals(executionId, that.executionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, executionId);
    }
}
