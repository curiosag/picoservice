package micro.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Env;
import micro.If.ExIf;

public class ExIfSerializer extends ExSerializer<ExIf> {

    public ExIfSerializer(Env env) {
        super(env);
    }

    @Override
    public void write(Kryo kryo, Output output, ExIf instance) {
        super.write(kryo, output, instance);
    }

    @Override
    public ExIf read(Kryo kryo, Input input, Class<? extends ExIf> ex) {
        ExIf result = new ExIf(env);
        super.read(kryo, input, result);
        return result;
    }

}
