package micro;

public class FPropagation {
    public final PropagationType propagationType;
    public final String nameReceived;
    public final String nameToPropagate;
    public final _F target;

    /**
     * If this.equals(target) a reflexive propagation will be established, meaning an execution propagates values to itself
     * */
    FPropagation(PropagationType propagationType, String nameReceived, String nameToPropagate, _F target) {
        this.propagationType = propagationType;
        this.nameReceived = nameReceived;
        this.nameToPropagate = nameToPropagate;
        this.target = target;
    }

}