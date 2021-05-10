package micro;

public class Check {

    public static <T> T notNull(T o) {
        if (o == null) {
            throw new IllegalArgumentException();
        }
        return o;
    }

    public static void invariant(boolean invariant) {
        if (!invariant) {
            fail("eh...");
        }
    }

    public static void condition(boolean condition) {
        if (!condition) {
            fail("eh...");
        }
    }

    public static void preCondition(boolean condition) {
        if (!condition) {
            fail("eh...");
        }
    }

    public static void invariant(boolean invariant, String msg) {
        if (!invariant) {
            fail(msg);
        }
    }

    public static void argument(boolean condition, String msg) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void fail(String msg) {
        throw new IllegalStateException(msg);
    }

}
