package micro;

public class FPropagation {
    final PropagationType propagationType;
    final String nameReceived;
    public final String nameToPropagate;
    final _F target;

    public FPropagation(PropagationType propagationType, String nameReceived, String nameToPropagate, _F target) {
        this.propagationType = propagationType;
        this.nameReceived = nameReceived;
        this.nameToPropagate = nameToPropagate;
        this.target = target;
    }

    FPropagation(String nameReceived, String nameToPropagate, _F target) {
        this(PropagationType.INDISCRIMINATE, nameReceived, nameToPropagate, target);
    }

}