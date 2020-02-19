package micro;

public class Check {

    public static Object notNull(Object o){
        if (o == null) {
            throw new IllegalArgumentException();
        }
        return o;
    }
}
