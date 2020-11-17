package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;

import java.io.Closeable;
import java.io.File;

import static micro.event.KryoStuff.createOutput;

public class EventLogWriter implements Closeable {
    private final Kryo kryo = new Kryo();
    private final Output output;

    private final String filename;

    public EventLogWriter(String filename, boolean clear) {
        this.filename = filename;
        if (clear) {
            deleteFile();
        }
        output = createOutput(filename);
    }

    public synchronized void put(Event e) {
        try {
            KryoSerializedClass.writeObject(kryo, output, e);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    public synchronized void flush() {
        output.flush();
    }

    private void deleteFile() {
        //noinspection ResultOfMethodCallIgnored
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
