package micro.primitives;

import micro.Check;
import micro.Value;

public class Print extends Action {

    public Print() {
        super(values -> {
        Check.notNull(values);
        for(Value v: values.values())
        {
            System.out.println(v.getName() + " " + v.get());
        }
        });
    }

    public static Print print(){
        return new Print();
    }
}
