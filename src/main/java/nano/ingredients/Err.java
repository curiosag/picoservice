package nano.ingredients;

import java.io.Serializable;

public class Err  implements Serializable {
    private static final long serialVersionUID = 0L;

    public final Function function;
    public final Message message;
    public final java.lang.Exception exception;

    public Err(Function function, Message message, java.lang.Exception exception) {
        this.function = function;
        this.message = message;
        this.exception = exception;
    }
}
