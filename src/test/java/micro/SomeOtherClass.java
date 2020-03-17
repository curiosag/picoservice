package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class SomeOtherClass extends SomeClass implements KryoSerializable {
    private long m;

    public SomeOtherClass() {
    }

    public SomeOtherClass(long l, long m) {
        super(l);
        this.m = m;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeVarLong(m, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        m = input.readVarLong(true);
    }
}
