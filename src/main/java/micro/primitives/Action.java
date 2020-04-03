package micro.primitives;

import micro.Value;

import java.util.List;
import java.util.function.Consumer;

public class Action implements SideEffect {

    private final Consumer<List<Value>> effect;

    public Action(Consumer<List<Value>> effect){
        this.effect = effect;
    }

    @Override
    public Object execute(List<Value> parameters) {
        effect.accept(parameters);
        return null;
    }

}