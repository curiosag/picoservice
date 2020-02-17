package micro.atomicFunctions;

import micro.Value;

import java.util.List;

public class SubInt implements Atom {

    @Override
    public Object execute(List<Value> parameters) {

        if (parameters == null || parameters.size() != 2) {
            throw new IllegalArgumentException();
        }

        for (Value v : parameters)
            if (!(v.get() instanceof Integer)) {
                throw new IllegalArgumentException();
            }

        return (Integer) parameters.get(0).get() - (Integer) parameters.get(1).get();
    }

}
