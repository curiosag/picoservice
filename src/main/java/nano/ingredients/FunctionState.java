package nano.ingredients;

import java.io.Serializable;

public class FunctionState implements Serializable {
    private static final long serialVersionUID = 0L;
    final Origin origin;

    public FunctionState(Origin origin) {
        this.origin = origin;
    }


}
