package miso.ingredients;

import java.util.Objects;

public class Source {
    public final Func<?> host;
    public final Long executionId;
    public final Integer callLevel;

    public Source(Func<?> host, Long executionId, Integer callLevel) {
        this.host = host;
        this.executionId = executionId;
        this.callLevel = callLevel;
    }

    public static Source opId(Func<?> host, Long executionId, Integer callLevel, String resultKeyExpected) {
        return new Source(host, executionId, callLevel);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) %d/%d -> %s", host.getClass().getSimpleName(), host.address.value, executionId, callLevel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Source source = (Source) o;
        return Objects.equals(host, source.host) &&
                Objects.equals(executionId, source.executionId) &&
                Objects.equals(callLevel, source.callLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, executionId, callLevel);
    }

    public Source decCallLevel() {
        return withCallLevel( callLevel - 1);
    }

    public Source incCallLevel() {
        return withCallLevel( callLevel + 1);
    }

    public Source withHost(Func<?> host) {
        return new Source(host, executionId, callLevel);
    }

    private Source withCallLevel(Integer level) {
        return new Source(host, executionId, level);
    }
}
