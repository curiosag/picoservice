package micro.primitives;

import micro.Check;
import micro.Names;
import micro.Value;

import java.util.List;
import java.util.Objects;

public class Eq implements Primitive {

    @Override
    public Object execute(List<Value> values) {
        Check.notNull(values);
        return Objects.equals(getValue(Names.left, values), getValue(Names.right, values));
    }

    public static Eq eq (){
        return new Eq();
    }
}
