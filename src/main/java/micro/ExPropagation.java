package micro;

public class ExPropagation {

    private final FPropagation template;
    private final _Ex to;

    public PropagationType getPropagationType(){
        return template.propagationType;
    }

    public String getNameToPropagate(){
        return template.nameToPropagate;
    }

    String getNameReceived(){
        return template.nameReceived;
    }

    ExPropagation(FPropagation template, _Ex to) {
        this.template = template;
        this.to = to;
    }

    public _Ex getTo() {
        return to;
    }

}