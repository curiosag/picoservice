package miso.ingredients;

public class Nop<V> extends Function<V> {

    public static Nop nop = new Nop<>();

    @Override
    protected boolean isParameter(String key) {
        return false;
    }

    @Override
    public void recieve(Message message) {
    }

    @Override
    State newState(Source source) {
        throw new IllegalStateException();
    }

    @Override
    protected void processInner(Message m, State s) {
    }

}
