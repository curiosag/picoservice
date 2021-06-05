package micro.experiments;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.junit.Test;

public class SerializationTest {

    @Test
    public void la() {
        Kryo kryo = new Kryo();
        kryo.register(SomeClass.class);
        kryo.register(SomeOtherClass.class);

        kryo.register(SomeOtherClass.class, new SomeSerializer());

        Output output = new Output(256);
        Input input = new Input(output.getBuffer());

        SomeOtherClass c = new SomeOtherClass(0);
        c.setM(1);
        c.setl(2);

        kryo.writeClassAndObject(output, c);
        Object object2 = kryo.readClassAndObject(input);

        kryo.reset();
        output.reset();
        input.reset();

    }

}
