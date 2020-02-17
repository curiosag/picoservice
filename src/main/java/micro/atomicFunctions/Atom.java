package micro.atomicFunctions;

import micro.Value;

import java.util.List;

public interface Atom {
    Object execute(List<Value> actualParameters);

    default boolean isSideEffect(){
        return false;
    };
}
