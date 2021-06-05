package micro;

import java.util.function.Supplier;

public class Concurrent {

    public static void sleep(int millis) {
        if (millis > 0)
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
    }

    public static void await(int checkEach, Supplier<Boolean> condition) {
        while (!condition.get()) {
            await(checkEach);
        }
    }

    public static void await(Supplier<Boolean> condition) {
        while (!condition.get()) {
            await(50);
        }
    }

    public static void await(int millis) {
        sleep(millis);
    }

}
