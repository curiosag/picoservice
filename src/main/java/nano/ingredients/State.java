package nano.ingredients;

import java.io.Serializable;

public class State  implements Serializable {
    private static final long serialVersionUID = 0L;
    final Origin origin;

    public State(Origin origin) {
        this.origin = origin;
    }


}
