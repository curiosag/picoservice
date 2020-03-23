package micro;

import java.util.Objects;

public class EnvExMatcher {
    public final _Ex returnTo;
    public final _F f;

    public EnvExMatcher(_Ex returnTo, _F f) {
        this.returnTo = returnTo;
        this.f = f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnvExMatcher that = (EnvExMatcher) o;
        return Objects.equals(returnTo, that.returnTo) &&
                Objects.equals(f, that.f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnTo, f);
    }
}
