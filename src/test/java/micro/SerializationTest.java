package micro;

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

        Output output = new Output(256);
        Input input = new Input(output.getBuffer());

        SomeClass c = new SomeOtherClass(1,2);
        kryo.writeClassAndObject(output, c);
        Object object2 = kryo.readClassAndObject(input);

        kryo.reset();
        output.reset();
        input.reset();

        c = new SomeOtherClass(3,4);
        kryo.writeClassAndObject(output, c);
        object2 = kryo.readClassAndObject(input);
        kryo.reset();
    }

}
