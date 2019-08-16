package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.Collections;
import java.util.List;

public class FunctionCall<T> extends Func<T> {

    private final Func<T> function;

    public FunctionCall(Func<T> f) {
        this.function = f;
    }

    @Override
    State newState(Source source) {
        return new State(source);
    }

    @Override
    List<String> keysExpected() {
        return Collections.emptyList();
    }

    public static <T> FunctionCall call(Func<T> f) {
        return new FunctionCall<>(f);
    }

    @Override
    protected void process(Message m) {
        if (m.hasKey(Name.functionResult)) {
            Message functionResult = new Message(returnKey, m.value, new Source(this, m.source.executionId, m.source.callLevel - 1));
            returnTo.recieve(functionResult);
        } else {
            Source source = new Source(this, m.source.executionId, m.source.callLevel + 1);
            Message callParameter = new Message(m.key, m.value, source);
            function.recieve(callParameter);
        }
    }

}
