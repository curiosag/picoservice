package micro;

public class Check {

    public static Object notNull(Object o) {
        if (o == null) {
            throw new IllegalArgumentException();
        }
        return o;
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
