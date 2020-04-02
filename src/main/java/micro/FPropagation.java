package micro;

class FPropagation {
    final PropagationType propagationType;
    final String nameReceived;
    final String nameToPropagate;
    final _F target;

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