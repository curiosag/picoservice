package micro.event.serialization;

import java.io.Closeable;

public interface Outgoing extends Closeable {

    void writeVarLong(long senderId, boolean b);

    void writeString(String name);

    void writeVarInt(int anInt, boolean b);

    void writeBoolean(Boolean value);
}
