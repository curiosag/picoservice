package micro.experiments;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SomeSerializer extends Serializer<SomeOtherClass> {

    @Override
    public void write(Kryo kryo, Output output, SomeOtherClass o) {
        o.write(kryo, output);
    }

    @Override
    public SomeOtherClass read(Kryo kryo, Input input, Class<? extends SomeOtherClass> o) {
        SomeOtherClass result = new SomeOtherClass(-1);
        result.read(kryo, input);
        return result;
    }
}
