package miso.ingredients;

import miso.message.Message;

import java.util.Collections;
import java.util.List;
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

    @Override
    State newState(Source source) {
        return new State(source);
    }

    @Override
    List<String> keysExpected() {
        return Collections.emptyList();
    }
}
