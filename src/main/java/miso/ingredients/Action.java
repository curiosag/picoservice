package miso.ingredients;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Action extends Function {

    private final Consumer<Message> action;
    private final List<String> expectedParams = new ArrayList<>();

    private Action(Consumer<Message> action) {
        this.action = action;
    }

    @Override
    public boolean isParameter(String key) {
        return expectedParams.contains(key);
    }

    @Override
    protected void processInner(Message m, State s) {
        action.accept(m);
    }

    public static Action action(Consumer<Message> action) {
        Action result = new Action(action);
        start(result);
        return result;
    }

    @Override
    State newState(Source source) {
        return new State(source);
    }


    public void param(String key) {
        expectedParams.add(key);
    }
}
