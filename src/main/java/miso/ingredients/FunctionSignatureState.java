package miso.ingredients;

public class FunctionSignatureState extends State {

    public Function<?> triggerOfCaller;

    public Origin caller;

    public boolean partialApplicationValuesForwarded;

    public FunctionSignatureState(Origin origin) {
        super(origin);
    }

    public void setPartialApplicationValuesForwarded(boolean partialApplicationValuesForwarded) {
        this.partialApplicationValuesForwarded = partialApplicationValuesForwarded;
    }
}
