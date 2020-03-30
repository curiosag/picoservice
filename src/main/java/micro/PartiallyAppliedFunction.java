package micro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PartiallyAppliedFunction {

    public F baseFunction;
    public List<Value> partialValues;

    public PartiallyAppliedFunction(F baseFunction, Collection<Value> partialValues) {
        this.baseFunction = baseFunction;
        this.partialValues = new ArrayList<>(partialValues);
    }

}
