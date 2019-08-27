package miso.ingredients;

import miso.misc.Name;

import static miso.ingredients.Message.message;
import static miso.ingredients.Source.source;

public class Call<T> extends Function<T> {

    private final Function<T> function;

    protected Call(Function<T> f) {
        this.function = f;
    }

    public static <T> Call<T> call(Function<T> body){
        Call<T> result = new Call<>(body);
        start(result);
        return result;
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
            Message result = message(returnKey, m.value, source(this, m.source.executionId, m.source.callLevel - 1));
            returnTo.recieve(result);
        } else {
            Source source = source(this, m.source.executionId, m.source.callLevel + 1);
            Message callParameter = message(m.key, m.value, source);
            function.recieve(callParameter);
        }
    }

}
