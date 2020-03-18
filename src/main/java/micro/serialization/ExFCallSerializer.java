package micro.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Env;
import micro.ExFCall;

public class ExFCallSerializer extends ExSerializer<ExFCall> {

    public ExFCallSerializer(Env env) {
        super(env);
    }

    @Override
    public void write(Kryo kryo, Output output, ExFCall instance) {
        super.write(kryo, output, instance);
    }

    @Override
    public ExFCall read(Kryo kryo, Input input, Class<? extends ExFCall> ex) {
        ExFCall result = new ExFCall(env);

        return result;
    }

}
