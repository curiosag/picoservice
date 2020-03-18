package micro.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Env;
import micro.ExF;
import micro.ExFCall;
import micro.If.ExIf;

public class Serialization {
    private final Env env;

    Kryo kryo = new Kryo();
    Output output = new Output(256);
    Input input = new Input(output.getBuffer());

    public Serialization(Env env) {
        this.env = env;
        kryo.register(ExF.class, new ExFSerializer(env));
        kryo.register(ExFCall.class, new ExFCallSerializer(env));
        kryo.register(ExIf.class, new ExIfSerializer(env));
    }

}
