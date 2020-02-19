package micro;

public class FPropagation {
    final String nameReceived;
    public final String nameToPropagate;
    final _F target;

    protected FPropagation(String nameReceived, String nameToPropagate, _F target) {
        this.nameReceived = nameReceived;
        this.nameToPropagate = nameToPropagate;
        this.target = target;
    }

}