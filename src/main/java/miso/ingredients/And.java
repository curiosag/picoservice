package miso.ingredients;

import miso.Actress;

import java.util.Arrays;
import java.util.List;

public class And extends BinOp<Boolean, Boolean> {
    public And() {
        super((n1, n2) -> n1 & n2, o -> (Boolean) o);
    }

    public And and() {
        return new And();
    }
}
