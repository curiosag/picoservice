package nano.ingredients;

import java.util.function.Supplier;

public class AsyncStuff {

    public static void await(int checkEach, Supplier<Boolean> condition) {
        while (!condition.get()) {
            await(checkEach);
        }
    }

    public static void await(Supplier<Boolean> condition) {
        while (!condition.get()) {
            await(10);
        }
    }

    public static void await(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //
        }
    }
}
