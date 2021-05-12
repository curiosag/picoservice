package micro.event.eventlog;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;
import micro.event.Event;

import java.io.File;

import static micro.event.eventlog.KryoStuff.createOutput;

public class KryoEventLogWriter implements EventLogWriter {
    private final Kryo kryo = new Kryo();
    private final Output output;

    private final String filename;

    public KryoEventLogWriter(String filename, boolean clear) {
        this.filename = filename;
        if (clear) {
            deleteFile();
        }
        output = createOutput(filename);
    }

    @Override
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
