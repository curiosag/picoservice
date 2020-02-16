package micro;

class FPropagation {
    final String nameReceived;
    final String nameToPropagate;
    final F target;

    FPropagation(String nameReceived, String nameToPropagate, F target) {
        this.nameReceived = nameReceived;
        this.nameToPropagate = nameToPropagate;
        this.target = target;
    }

}