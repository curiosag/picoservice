package micro.event.serialization;

import java.io.IOException;
import java.util.Queue;

public class MemIncoming implements Incoming {

    private final Queue<Object> serializedValues;

    public MemIncoming(Queue<Object> serializedValues) {
        this.serializedValues = serializedValues;
    }

    private Object getNext() {
        return serializedValues.poll();
    }

    @Override
    public long readVarLong(boolean b) {
        return (Long) getNext();
    }

    @Override
    public int readVarInt(boolean b) {
        return (Integer) getNext();
    }

    @Override
    public String readString() {
        return (String) getNext();
    }

    @Override
    public boolean readBoolean() {
        return (Boolean) getNext();
    }

    @Override
    public void close() throws IOException {

    }
}
