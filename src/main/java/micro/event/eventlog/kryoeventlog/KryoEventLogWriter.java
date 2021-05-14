package micro.event.eventlog.kryoeventlog;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;
import micro.event.Event;
import micro.event.eventlog.EventLogWriter;
import micro.event.serialization.SerioulizedEvent;
import micro.event.serialization.KryoOutgoing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class KryoEventLogWriter implements EventLogWriter {
    private final KryoOutgoing output;

    private final String filename;

    public KryoEventLogWriter(String filename, boolean clear) {
        this.filename = filename;
        if (clear) {
            deleteFile();
        }
        output = createOutput(filename);
    }

    private KryoOutgoing createOutput(String filename) {
        try {
            //noinspection ResultOfMethodCallIgnored
            new File(filename).createNewFile();
            return new KryoOutgoing(new Output(new FileOutputStream(filename, true)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void put(Event e) {
        try {
            SerioulizedEvent.writeObject(output, e);
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
