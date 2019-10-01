package nano.ingredients;

public class FunctionSignatureState extends State {

    public boolean partialApplicationValuesForwarded;

    public FunctionSignatureState(Origin origin) {
        super(origin);
    }

    public void setPartialApplicationValuesForwarded(boolean partialApplicationValuesForwarded) {
        this.partialApplicationValuesForwarded = partialApplicationValuesForwarded;
    }

}
