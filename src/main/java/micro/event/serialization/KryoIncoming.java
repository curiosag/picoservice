package micro.event.serialization;

import com.esotericsoftware.kryo.io.Input;

import java.io.IOException;

public class KryoIncoming implements Incoming{

    private final Input input;

    public KryoIncoming(Input input) {
        this.input = input;
    }

    @Override
    public long readVarLong(boolean b) {
        return input.readVarLong(b);
    }

    @Override
    public int readVarInt(boolean b) {
        return input.readVarInt(b);
    }

    @Override
    public String readString() {
        return input.readString();
    }

    @Override
    public boolean readBoolean() {
        return input.readBoolean();
    }

    @Override
    public void close() throws IOException {

    }
}
