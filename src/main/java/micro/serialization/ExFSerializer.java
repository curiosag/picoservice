package micro.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Env;
import micro.ExF;

public class ExFSerializer extends ExSerializer<ExF> {

    public ExFSerializer(Env env) {
        super(env);
    }

    @Override
    public void write(Kryo kryo, Output output, ExF instance) {
        super.write(kryo, output, instance);
    }

    @Override
    public ExF read(Kryo kryo, Input input, Class<? extends ExF> ex) {
        ExF result = new ExF(env);
        super.read(kryo, input, result);
        return result;
    }

}
