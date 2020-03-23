package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;

import java.io.Closeable;
import java.io.File;

import static micro.event.KryoStuff.createOutput;

public class EventLogWriter implements Closeable {
    private Kryo kryo = new Kryo();
    private Output output;

    private final String filename;

    public EventLogWriter(String filename, boolean clear) {
        this.filename = filename;
        if (clear) {
            deleteFile();
        }
        output = createOutput(filename);
    }

    public void put(Event e) {
        KryoSerializedClass.writeObject(kryo, output, e);
    }

    public void flush() {
        output.flush();
    }

    private void deleteFile() {
        new File(filename).delete();
    }

    @Override
    public void close() {
        try {
            output.close();
        } catch (KryoException e) {
            throw new RuntimeException(e);
        }
    }
}
