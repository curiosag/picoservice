package micro.primitives;

import micro.Value;

import java.util.Map;
import java.util.function.Consumer;

public class Action implements SideEffect {

    private final Consumer<Map<String, Value>> effect;

    public Action(Consumer<Map<String, Value>> effect){
        this.effect = effect;
    }

    @Override
    public Object execute(Map<String, Value> parameters) {
        effect.accept(parameters);
        return null;
    }

}