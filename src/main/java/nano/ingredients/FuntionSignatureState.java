package nano.ingredients;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class FuntionSignatureState extends FunctionState {

    private final List<String> paramList;
    private final List<String> letKeys;

    final HashMap<String, Serializable> paramValues = new HashMap<>();

    public boolean paramsPropagated;

    public FuntionSignatureState(Origin origin, List<String> paramList, List<String> letKeys) {
        super(origin);
        this.paramList = paramList;
        this.letKeys = letKeys;
    }

    public void addParamValue(String key, Serializable value) {
        if (!letKeys.contains(key) &&
                !Function.in(key, Name.kickOff, Name.removePartialAppValues, Name.error, Name.ack))
            if (!paramList.contains(key) || (paramValues.get(key)!= null && ! paramValues.get(key).equals(value))) {
                throw new IllegalStateException();
            }
        paramValues.put(key, value);
    }

    public boolean paramValuesComplete() {
        return paramValues.size() == paramList.size();
    }
}
