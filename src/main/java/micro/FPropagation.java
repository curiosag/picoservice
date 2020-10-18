package micro;

public class FPropagation {
    public final PropagationType propagationType;
    final String nameReceived;
    public String nameToPropagate;
    public final _F target;

    FPropagation(PropagationType propagationType, String nameReceived, String nameToPropagate, _F target) {
        this.propagationType = propagationType;
        this.nameReceived = nameReceived;
        this.nameToPropagate = nameToPropagate;
        this.target = target;
    }

    @Override
    public String toString() {
        return "{\"FPropagation\":{" +
                "\"propagationType\":\"" + propagationType +'\"' +
                ", \"nameReceived\":\"" + nameReceived + '\"' +
                ", \"nameToPropagate\":\"" + nameToPropagate + '\"' +
                ", \"target\":" + target.getId() +
                "}}";
    }
}