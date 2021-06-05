package micro.event.eventlog.kryoeventlog;

import com.esotericsoftware.kryo.io.Input;
import micro.event.eventlog.EventLogIterator;
import micro.event.eventlog.EventLogReader;
import micro.event.serialization.KryoIncoming;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class KryoEventLogReader implements EventLogReader {

    private final String filename;

    public KryoEventLogReader(String filename) {
        this.filename = filename;
    }

    @Override
    public EventLogIterator iterator() {
        return new EventLogIterator(createInput(filename));
    }

    private KryoIncoming createInput(String filename) {
        try {
            //noinspection ResultOfMethodCallIgnored
            new File(filename).createNewFile();
            return new KryoIncoming(new Input(new FileInputStream(filename)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
