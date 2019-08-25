package miso.ingredients;

import miso.message.Message;

import static miso.ingredients.Nop.nop;

public class Const extends FunctionCall<Integer> {

    private Integer value;

    public Const(Integer value) {
        super (nop);
        this.value = value;
    }

    public static Const constVal(Integer value){
        return new Const(value);
    }

    @Override
    public void recieve(Message message) {
        returnTo.recieve(new Message(returnKey, value, message.source.withHost(this)));
    }

    @Override
    public void run() {
        throw new IllegalStateException();
    }

    @Override
    State newState(Source source) {
        throw new IllegalStateException();
    }

    @Override
    protected void processInner(Message message, State s) {
        throw new IllegalStateException();
    }
}
