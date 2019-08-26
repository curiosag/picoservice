package miso.ingredients;

import java.util.Objects;

public class Source {
    public final Function<?> host;
    public final Long executionId;
    public final Integer callLevel;

    private Source(Function<?> host, Long executionId, Integer callLevel) {
        this.host = host;
        this.executionId = executionId;
        this.callLevel = callLevel;
    }

    public static Source source(Function<?> host, Long executionId, Integer callLevel) {
        return new Source(host, executionId, callLevel);
    }

    @Override
    public String toString() {
        return String.format("Source: %s (%s) %d/%d ", host.getClass().getSimpleName(), host.address.value, executionId, callLevel);
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

    Source withHost(Function<?> host) {
        return source(host, executionId, callLevel);
    }
}
