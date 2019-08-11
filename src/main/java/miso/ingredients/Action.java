package miso.ingredients;

import miso.message.Message;

import java.util.function.Consumer;

public class Action extends Func {

    private final Consumer<Message> action;

    private Action(Consumer<Message> action) {
        this.action = action;
    }

    @Override
    protected void process(Message m) {
        action.accept(m);
    }

    public static Action action(Consumer<Message> action) {
        return new Action(action);
    }
}
