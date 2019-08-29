package miso.ingredients;

import static miso.ingredients.Message.message;
import static miso.ingredients.Nop.nop;

public class Const extends Call<Integer> {

    private Integer value;

    public Const(Integer value) {
        super(new Nop<>());
        this.value = value;
    }

    public static Const constant(Integer value){
        return new Const(value);
    }

    @Override
    public void recieve(Message message) {
        returnTo.recieve(message(returnKey, value, message.source.withHost(this)));
    }

    @Override
    public void run() {
        throw new IllegalStateException();
    }

    @Override
    protected State newState(Source source) {
        throw new IllegalStateException();
    }

    @Override
    protected void processInner(Message message, State s) {
        throw new IllegalStateException();
    }
}
