package nano.ingredients.guards;

import nano.ingredients.ComputationBough;

import java.util.Collection;
import java.util.Map;

public class Guards {
    public static <T> T notNull(T value) {
        if (value == null) {
            throw new IllegalStateException();
        }
        return value;
    }

    public static <T extends Collection<?>> T empty(T value) {
        if (!notNull(value).isEmpty()) {
            throw new IllegalStateException();
        }
        return value;
    }

    public static <T extends Collection<?>> T notEmpty(T value) {
        if (notNull(value).isEmpty()) {
            throw new IllegalStateException();
        }
        return value;
    }

    public static <T extends Map<?, ?>> T notEmpty(T value) {
        if (notNull(value).isEmpty()) {
            throw new IllegalStateException();
        }
        return value;
    }

    public static void isFalse(boolean v) {
        if (v) {
            throw new IllegalStateException();
        }
    }

    public static void isTrue(boolean v) {
        if (!v) {
            throw new IllegalStateException();
        }
    }

    public static void isEmpty(ComputationBough computationBough) {
        isTrue(computationBough.isEmpty());
    }
}
