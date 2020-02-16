package micro;

import java.util.List;

public interface FAtom {
    Object execute(List<Value> actualParameters);

    default boolean isSideEffect(){
        return false;
    };
}
