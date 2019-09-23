package miso.ingredients;

import java.util.Objects;

public class PartialAppMatcher {
    public final Origin origin;

    private PartialAppMatcher(Origin origin) {
        this.origin = origin;
    }

    public static PartialAppMatcher matcher(Origin o){
        return new PartialAppMatcher(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartialAppMatcher that = (PartialAppMatcher) o;
        return Objects.equals(origin.executionId, that.origin.executionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin.executionId);
    }
}
