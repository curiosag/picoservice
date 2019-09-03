package miso.ingredients;

import static miso.ingredients.Actresses.start;
import static miso.ingredients.Message.message;

public class FunctionCall<T> extends Function<T> {

    private final Function<T> function;

    protected FunctionCall(Function<T> f) {
        this.function = f;
    }

    public static <T> FunctionCall<T> functionCall(Function<T> body) {
        FunctionCall<T> result = new FunctionCall<>(body);
        start(result);
        return result;
    }

    @Override
    protected boolean isParameter(String key) {
        return true;
    }

    @Override
    protected State newState(Origin origin) {
        throw new IllegalStateException();
    }

    @Override
    protected void processInner(Message m, State state) {
        throw new IllegalStateException();
    }

    @Override
    protected void process(Message m) {
        maybeTrace(m);
        if (m.hasKey(Name.result)) {
            returnTo.receive(message(returnKey, m.value, m.origin.sender(this)));
        } else {
            function.receive(m.sender(this));
        }
    }

}
