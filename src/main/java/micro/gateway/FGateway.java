package micro.gateway;

import micro.PropagationType;
import micro._Ex;
import micro._F;

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
    public _Ex createExecution(long id, _Ex returnTo) {
        throw new IllegalStateException();
    }
}
