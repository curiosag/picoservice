package nano.ingredients;

import static nano.ingredients.Message.message;

public class Const extends Function<Integer> {

    private Integer value;

    public Const(Integer value) {
        this.value = value;
    }

    public static Const constant(Integer value) {
        return new Const(value);
    }

    @Override
    public void tell(Message message) {
        returnTo.tell(message(returnKey, value, message.origin.sender(this)));
    }

    @Override
    protected FunctionState newState(Origin origin) {
        throw new IllegalStateException();
    }

    @Override
    protected boolean shouldPropagate(String key) {
        throw new IllegalStateException();
    }

    @Override
    protected void processInner(Message message, FunctionState s) {
        throw new IllegalStateException();
    }
}