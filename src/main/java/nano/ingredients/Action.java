package nano.ingredients;



import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static nano.ingredients.Ensemble.attachActor;
import static nano.ingredients.Nop.nop;

public class Action extends Function {

    private Consumer<Message> action;
    private final List<String> expectedParams = new ArrayList<>();

    protected Action(Consumer<Message> action) {
        this.action = action;
    }

    public Action() {
        super();
        Ensemble.instance().enlist(this);
    }

    @Override
    public boolean shouldPropagate(String key) {
        return ! expectedParams.contains(key);
    }

    @Override
    protected void processInner(Message m, FunctionState s) {
        if (action == null)
        {
            throw new IllegalStateException();
        }
        action.accept(m);
        removeState(s.origin);
    }

    public static Action action(Consumer<Message> action) {
        Action result = new Action(action);
        result.returnTo(nop, Name.nop);
        attachActor(result);
        return result;
    }

    public Consumer<Message> getAction() {
        return action;
    }

    public void setAction(Consumer<Message> action) {
        this.action = action;
    }

    @Override
    protected FunctionState newState(Origin origin) {
        return new FunctionState(origin);
    }


    public Action param(String key) {
        expectedParams.add(key);
        return this;
    }
}
