package miso.ingredients;

public class FunctionSignatureState extends State {

    public boolean partialApplicationValuesForwarded;

    public FunctionSignatureState(Origin origin) {
        super(origin);
    }

    public void setPartialApplicationValuesForwarded(boolean partialApplicationValuesForwarded) {
        this.partialApplicationValuesForwarded = partialApplicationValuesForwarded;
    }

    public Function<?> getTriggerOfCaller() {
        return origin.triggeredBy;
    }

    public Origin getCaller() {
        return origin;
    }

}
