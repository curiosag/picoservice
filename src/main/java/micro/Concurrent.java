package micro;

public class Concurrent {

    public static void sleep(int millis) {
        if (millis > 0)
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
    }

}
