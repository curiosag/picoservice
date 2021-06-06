package micro;


import java.io.Serializable;

public class Err  implements Serializable {
    private static final long serialVersionUID = 0L;


    public final java.lang.Exception exception;

    public Err(java.lang.Exception exception) {
        this.exception = exception;
    }
}
