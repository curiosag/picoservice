package micro;

public interface _F extends Id {

    /**
     *
     * If this.equals(target) a reflexive propagation will be established, meaning an execution xf of F propagates values to itself
     *
     * */
    void addPropagation(PropagationType type, String nameExpected, String namePropagated, _F to);

    _Ex createExecution(long id, _Ex returnTo);

}
