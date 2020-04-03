package micro.primitives;

import micro.Check;
import micro.Value;

import java.util.List;
import java.util.stream.Collectors;

public interface Primitive {

    Primitive nop = parameters -> null;

    default boolean isSideEffect(){
        return false;
    };

    Object execute(List<Value> parameters);

    default Object getValue(String name, List<Value> values){
        List<Value> result = values.stream().filter(i -> i.getName().equals(name)).collect(Collectors.toList());
        Check.invariant(result.size() == 1, "invalid number of parameters found for name " + name);
        return result.get(0).get();
    }
}
