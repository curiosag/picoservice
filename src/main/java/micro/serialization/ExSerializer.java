package micro.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Env;
import micro.Ex;

public abstract class ExSerializer<T extends Ex> extends Serializer<T> {

    final Env env;

    public ExSerializer(Env env) {
        this.env = env;
    }

    @Override
    public void write(Kryo kryo, Output output, T instance) {
        instance.write(kryo, output);
    }

    void read(Kryo kryo, Input input, T instance) {
        instance.read(kryo, input);
    }

}
