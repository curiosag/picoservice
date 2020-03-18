package micro.experiments;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class SomeOtherClass extends SomeClass implements KryoSerializable {
    private long m;

    private SomeOtherClass() {
    }

    public SomeOtherClass(long i) {
        super(i);
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

    public long getM() {
        return m;
    }

    public void setM(long m) {
        this.m = m;
    }
}
