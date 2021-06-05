package micro.experiments;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SomeClass implements KryoSerializable {
    private long l;
    private long i;

    public long getl() {
        return l;
    }

    public void setl(long l) {
        this.l = l;
    }

    protected SomeClass() {
    }

    public SomeClass(long i) {
        this.i = i;
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
