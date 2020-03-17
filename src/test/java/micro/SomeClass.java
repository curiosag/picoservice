package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SomeClass implements KryoSerializable {
    private long l;

    public SomeClass(){
    }

    public SomeClass(long l) {
        this.l = l;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(l);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        l = input.readLong();
    }
}
