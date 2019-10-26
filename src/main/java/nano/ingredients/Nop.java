package nano.ingredients;

import java.io.Serializable;

public class Nop extends Function<Serializable> {

    public static Nop nop = createNop();

    private static Nop createNop() {
        Nop result = new Nop();
        result.address.value = -1L;
        result.address.label = "~~~~~~~~";
        return result;
    }
    @Override
    Address createAddress() {
        return new Address(-1L, "nop"); //can't have an assigned id, it would sabotage recoveries in test
    }

    @Override
    protected boolean shouldPropagate(String key) {
        return true;
    }

    @Override
    public void tell(Message message) {
    }

    @Override
    protected FunctionState newState(Origin origin) {
        throw new IllegalStateException();
    }

    @Override
    protected void processInner(Message m, FunctionState s) {
    }


}
