package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.Collections;
import java.util.List;

public class Function<T> extends Func<T> {

    private final Func<T> body;

    public Function(Func<T> body) {
        body.returnTo(Name.result, this);
        this.body = body;
    }

    @Override
    State newState(Source source) {
        return new State(source);
    }

    @Override
    List<String> keysExpected() {
        return Collections.emptyList();
    }

    @Override
    protected void process(Message m) {
        State state = getState(m.source);

        if (m.hasKey(Name.result)) {
            state.source.host.recieve(new Message(Name.functionResult, m.value, m.source.withHost(this)));
        } else {

        }
    }

}
