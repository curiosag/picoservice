package miso.ingredients;

import static miso.ingredients.Actresses.wire;
import static miso.ingredients.Message.message;

public class FunctionCall<T> extends Function<T> {

    private final Function<T> function;

    protected FunctionCall(Function<T> f) {
        this.function = f;
    }

    public static <T> FunctionCall<T> functionCall(Function<T> body) {
        FunctionCall<T> result = new FunctionCall<>(body);
        wire(result);
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
    public void process(Message message) {
        trace(message);

        Origin origin = message.origin.sender(this);
        if (message.hasKey(Name.result)) {
            removeState(origin);
            // returnResult((T) message.value, origin); doesn't work here, the popping messes it up
            // it must be hdlOnReturn, popCall, returnTo.tell
            hdlOnReturns(origin, onReturn);

            origin = origin.popCall();
            if (!this.address.id.equals(origin.lastPopped)) {
                throw new IllegalStateException();
            }
           returnTo.tell(message(returnKey, (T) message.value, origin));
        } else {
            if (!isConst(message)) // const already comes with proper stack
            {
                origin = origin.pushCall(this.address.id);
                getState(origin);
            }

            function.tell(message.origin(origin));
        }


    }

    private boolean isConst(Message message) {
        return message.origin.sender.equals(this);
    }

}
