package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

public class FunctionCallTarget<T> extends Function<T> {

    public FunctionCallTarget(Function<T> body) {
        body.returnTo(Name.functionResult, this);
    }

    @Override
    State newState(Source source) {
        return new State(source);
    }

    @Override
    protected boolean isParameter(String key) {
        return key.equals(Name.functionResult);
    }

    @Override
    protected void processInner(Message m, State state) {
        if (m.hasKey(Name.functionResult)) {
            state.source.host.recieve(new Message(Name.functionResult, m.value, m.source.withHost(this)));
        }
    }

}
