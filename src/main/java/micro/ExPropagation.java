package micro;

public class ExPropagation {

    private final FPropagation template;
    private final ExOnDemand to;

    public PropagationType getPropagationType() {
        return template.propagationType;
    }

    public String getNameToPropagate() {
        return template.nameToPropagate;
    }

    String getNameReceived() {
        return template.nameReceived;
    }

    ExPropagation(FPropagation template, ExOnDemand to) {
        this.template = template;
        this.to = to;
    }

    public ExOnDemand getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "{\"ExPropagation\":{" +
                "\"template\":" + template +
                ", \"to\":" + to.getId() +
                "}}";
    }
}