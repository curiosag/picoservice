package micro.event.serialization;

import java.io.Closeable;

public interface Incoming extends Closeable {

    long readVarLong(boolean b);

    int readVarInt(boolean b);

    String readString();

    boolean readBoolean();

}
