package micro;

public class FPropagation {
    final String nameReceived;
    public final String nameToPropagate;
    final F target;

    protected FPropagation(String nameReceived, String nameToPropagate, F target) {
        this.nameReceived = nameReceived;
        this.nameToPropagate = nameToPropagate;
        this.target = target;
    }

}