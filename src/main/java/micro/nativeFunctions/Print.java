package micro.nativeFunctions;

import micro.FSideEffect;
import micro.Value;
import micro.Void;

import java.util.List;

public class Print implements FSideEffect {

    @Override
    public Object execute(List<Value> values) {
        for(Value v: values)
        {
            System.out.println(v.getName() + " " + v.get());
        }
        return Void.aVoid;
    }
}
