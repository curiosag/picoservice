package miso.ingredients;

public class Nop extends Function<Object> {

    public static Nop nop = createNop();

    private static Nop createNop() {
        Nop result = new Nop();
        result.address.value = "nop";
        return result;
    }

    @Override
    protected boolean isParameter(String key) {
        return false;
    }

    @Override
    public void receive(Message message) {
    }

    @Override
    protected State newState(Origin origin) {
        throw new IllegalStateException();
    }

    @Override
    protected void processInner(Message m, State s) {
    }


}
