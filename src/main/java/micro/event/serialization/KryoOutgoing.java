package micro.event.serialization;

import com.esotericsoftware.kryo.io.Output;

public class KryoOutgoing implements Outgoing {

    private final Output output;

    public KryoOutgoing(Output output) {
        this.output = output;
    }

    @Override
    public void writeVarLong(long l, boolean b) {
        output.writeVarLong(l, b);
    }

    @Override
    public void writeString(String name) {
        output.writeString(name);
    }

    @Override
    public void writeVarInt(int anInt, boolean b) {
        output.writeVarInt(anInt, b);
    }

    @Override
    public void writeBoolean(Boolean value) {
        output.writeBoolean(value);
    }

    public void flush(){
        output.flush();
    }

    public void close(){
        output.close();
    }

}
