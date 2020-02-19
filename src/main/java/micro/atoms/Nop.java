package micro.atoms;

import micro.Value;

import java.util.Map;

public class Nop implements Atom {

    public static Nop nop = new Nop();

    @Override
    public Object execute(Map<String, Value> actualParameters) {
        throw new IllegalStateException();
    }

}
