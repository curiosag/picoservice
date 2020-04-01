package micro;

import java.util.Objects;

public class ExFMatcher {
    public final _Ex returnTo;
    public final _F f;

    public ExFMatcher(_Ex returnTo, _F f) {
        this.returnTo = returnTo;
        this.f = f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExFMatcher that = (ExFMatcher) o;
        return Objects.equals(returnTo, that.returnTo) &&
                Objects.equals(f, that.f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnTo, f);
    }
}
