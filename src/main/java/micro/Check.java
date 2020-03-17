package micro;

public class Check {

    public static Object notNull(Object o){
        if (o == null) {
            throw new IllegalArgumentException();
        }
        return o;
    }

    public static void invariant(boolean invariant, String msg)
    {
        if(! invariant)
        {
            throw new IllegalStateException(msg);
        }
    }
}
