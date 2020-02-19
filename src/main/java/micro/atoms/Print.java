package micro.atoms;

import micro.Check;
import micro.Value;
import micro.Void;

import java.util.Map;

public class Print implements SideEffect {

    @Override
    public Object execute(Map<String, Value> values) {
        Check.notNull(values);
        for(Value v: values.values())
        {
            System.out.println(v.getName() + " " + v.get());
        }
        return Void.aVoid;
    }

    public static Print print(){
        return new Print();
    }
}
