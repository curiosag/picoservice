package micro.gateway;

import micro.*;

import java.util.List;

public class FGateway implements _F {

    public final static FGateway instance = new FGateway();

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public void setId(long value) {

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

    @Override
    public void doneOn(String... params) {

    }

    @Override
    public String getLabel() {
        return "Gateway";
    }
}
