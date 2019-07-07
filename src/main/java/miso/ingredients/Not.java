package miso.ingredients;

import miso.Actress;

import java.util.Arrays;
import java.util.List;

public class Not extends UnOp<Boolean, Boolean> {
    public Not() {
        super((n) -> !n, o -> (Boolean) o);
    }

    public Not not() {
        return new Not();
    }
}
