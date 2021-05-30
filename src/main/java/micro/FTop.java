package micro;

import java.util.List;

import static micro.ExTop.TOP_ID;

public class FTop implements _F{
    @Override
    public long getId() {
        return TOP_ID;
    }

    @Override
    public void setId(long value) {
        throw new IllegalStateException();
    }

    @Override
    public void addPropagation(PropagationType type, String nameExpected, String namePropagated, _F to) {
        throw new IllegalStateException();
    }

    @Override
    public List<FPropagation> getPropagations() {
        return null;
    }

    @Override
    public _Ex createExecution(long id, _Ex returnTo) {
        throw new IllegalStateException();
    }

    @Override
    public Address getAddress() {
        return Address.localhost;
    }

    @Override
    public boolean isTailRecursive() {
        return false;
    }
}
