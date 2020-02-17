package micro.atomicFunctions;

import micro.Value;

import java.util.List;

public class Gt implements Atom {

    @Override
    public Object execute(List<Value> values) {

        if (values == null || values.size() != 2) {
            throw new IllegalArgumentException();
        }

        for (Value v : values)
            if (!(v.get() instanceof Comparable)) {
                throw new IllegalArgumentException();
            }

        return ((Comparable) values.get(0).get()).compareTo(values.get(1).get()) > 0;
    }

}
