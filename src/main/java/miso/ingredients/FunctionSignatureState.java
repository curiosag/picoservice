package miso.ingredients;

public class FunctionSignatureState extends State {

    public Function<?> triggerOfCaller;

    public FunctionSignatureState(Origin origin) {
        super(origin);
    }
}
