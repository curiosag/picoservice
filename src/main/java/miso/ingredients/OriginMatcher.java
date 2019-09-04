package miso.ingredients;

import java.util.Objects;

public class OriginMatcher {
    private final Function<?> scope;
    final Long executionId;
    private final Integer callLevel;

    private  OriginMatcher(Origin origin) {
        this.scope = origin.scope;
        this.executionId = origin.executionId;
        this.callLevel = origin.callLevel;
    }

    static OriginMatcher matcher(Origin o){
        return new OriginMatcher(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OriginMatcher that = (OriginMatcher) o;
        return Objects.equals(scope, that.scope) &&
                Objects.equals(executionId, that.executionId) &&
                Objects.equals(callLevel, that.callLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, executionId, callLevel);
    }
}
