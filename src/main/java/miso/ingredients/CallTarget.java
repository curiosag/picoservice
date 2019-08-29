package miso.ingredients;

import miso.misc.Name;

import static miso.ingredients.Message.message;

public class CallTarget<T> extends Function<T> {

    private CallTarget(Function<T> body) {
        body.returnTo(this, Name.functionResult);
    }

    public static <T> CallTarget<T> callTarget(Function<T> body){
        CallTarget<T> result = new CallTarget<>(body);
        start(result);
        return result;
    }

    @Override
    protected State newState(Source source) {
        return new State(source);
    }

    @Override
    protected boolean isParameter(String key) {
        return key.equals(Name.functionResult);
    }

    @Override
    protected void processInner(Message m, State state) {
        if (m.hasKey(Name.functionResult)) {
            removeState(state.source);
            state.source.host.recieve(message(Name.functionResult, m.value, m.source.withHost(this)));
        }
    }

}
