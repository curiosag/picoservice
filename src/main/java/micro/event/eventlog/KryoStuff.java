package micro.event.eventlog;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.*;

public class KryoStuff {

    public static Output createOutput(String filename) {
        try {
            assertFile(filename);
            return new Output(new FileOutputStream(filename, true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Input createInput(String filename) {
        try {
            assertFile(filename);
            return new Input(new FileInputStream(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertFile(String filename) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        new File(filename).createNewFile();
    }

}
