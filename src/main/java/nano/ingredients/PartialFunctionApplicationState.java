package nano.ingredients;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PartialFunctionApplicationState extends FunctionState {
    Map<String, Serializable> partialAppValues = new HashMap<>();

    public boolean partialApplicationValuesForwarded;

    public PartialFunctionApplicationState(Origin origin) {
        super(origin);
    }

    public void setPartialApplicationValuesForwarded(boolean partialApplicationValuesForwarded) {
        this.partialApplicationValuesForwarded = partialApplicationValuesForwarded;
    }

}
