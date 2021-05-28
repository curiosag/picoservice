package micro;

import java.util.List;

public interface _F extends Id {

    /**
     *
     * If this.equals(target) a reflexive propagation will be established, meaning an execution xf of F propagates values to itself
     *
     * */
    void addPropagation(PropagationType type, String nameExpected, String namePropagated, _F to);

    List<FPropagation> getPropagations();

    _Ex createExecution(long id, _Ex returnTo);

    Address getAddress();

    default boolean isLocal(){
        return getAddress().equals(Address.localhost);
    }

    boolean isTailRecursive();
}
