package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

public class FunctionCall<T> extends Function<T> {

    private final Function<T> function;

    public FunctionCall(Function<T> f) {
        this.function = f;
    }

    @Override
    protected boolean isParameter(String key) {
        return key.equals(Name.functionResult);
    }

    @Override
    State newState(Source source) {
        return new State(source);
    }

    @Override
    protected void processInner(Message m, State state) {

    }

    @Override
    protected void process(Message m) {
        if (m.hasKey(Name.functionResult)) {
            this.recieve(new Message(Name.finalizeComputation, null, m.source));
            Message result = new Message(returnKey, m.value, new Source(this, m.source.executionId, m.source.callLevel - 1));
            returnTo.recieve(result);

        } else {
            Source source = new Source(this, m.source.executionId, m.source.callLevel + 1);
            Message callParameter = new Message(m.key, m.value, source);
            function.recieve(callParameter);
        }
    }

}
