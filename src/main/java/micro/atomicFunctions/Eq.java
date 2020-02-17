package micro.atomicFunctions;

import micro.Value;

import java.util.List;
import java.util.Objects;

public class Eq implements Atom {

    @Override
    public Object execute(List<Value> values) {

        if (values == null || values.size() != 2) {
            throw new IllegalArgumentException();
        }

        return Objects.equals(values.get(0).get(), values.get(1).get());
    }

}
