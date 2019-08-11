package miso.ingredients;

import java.util.Objects;

public class OpId {
    public final Long executionId;
    public final Integer recursionLevel;

    public OpId(Long executionId, Integer recursionLevel) {
        this.executionId = executionId;
        this.recursionLevel = recursionLevel;
    }

    public static OpId opId(Long executionId, Integer recursionLevel){
        return new OpId(executionId, recursionLevel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpId opId = (OpId) o;
        return Objects.equals(executionId, opId.executionId) &&
                Objects.equals(recursionLevel, opId.recursionLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId, recursionLevel);
    }
}
