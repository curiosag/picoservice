package miso.ingredients;

public class Err {
    public final Function function;
    public final Message message;
    public final java.lang.Exception exception;

    public Err(Function function, Message message, java.lang.Exception exception) {
        this.function = function;
        this.message = message;
        this.exception = exception;
    }
}
