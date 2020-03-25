package micro.atoms;

import micro.Value;

import java.util.Map;

public interface Atom {

    Atom nop = parameters -> null;

    default boolean isSideEffect(){
        return false;
    };

    Object execute(Map<String, Value> parameters);
}
