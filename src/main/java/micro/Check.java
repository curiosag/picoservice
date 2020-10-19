package micro;

public class Check {

    public static <T> T notNull(T o) {
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

    public static void isLegitInputValue(Value v){
        Check.invariant(!(Names.result.equals(v.getName()) || Names.exception.equals(v.getName())), "result and exception expected to be processed in base class");
    }

}
