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
        return new State(origin);
    }

    @Override
    protected void processInner(Message m, State state) {
        throw new IllegalStateException();
    }

    @Override
    protected void process(Message message) {
        maybeTrace(message);

        Origin origin = message.origin.sender(this);

        if (message.hasKey(Name.result)) {
            removeState(origin);
            if (!origin.popCall().equals(this.address.id)) {
                throw new IllegalStateException();
            }
            returnTo.receive(message(returnKey, message.value, origin));
        } else {
            if (! isConst(message)) {
                origin.pushCall(this.address.id);
                getState(origin);
            }
            function.receive(message.origin(origin));
        }
    }

    private boolean isConst(Message message) {
        return message.origin.sender.equals(this);
    }

}
