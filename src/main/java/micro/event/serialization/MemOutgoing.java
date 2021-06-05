package micro.event.serialization;

import java.io.IOException;
import java.util.Queue;

public class MemOutgoing implements Outgoing{

    private final Queue<Object> values;

    public MemOutgoing(Queue<Object> values) {
        this.values = values;
    }

    @Override
    public void writeVarLong(long l, boolean b) {
        values.add(l);
    }

    @Override
    public void writeString(String s) {
        values.add(s);
    }

    @Override
    public void writeVarInt(int anInt, boolean b) {
        values.add(anInt);
    }

    @Override
    public void writeBoolean(Boolean value) {
        values.add(value);
    }

    @Override
    public void close() throws IOException {

    }
}
