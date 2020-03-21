package micro.exevent;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Ex;
import micro.Value;

public class ValueProcessedEvent implements ExEvent {
    private final Ex ex;
    public final String valueName;

    public ValueProcessedEvent(Ex ex, String valueName) {
        this.ex = ex;
        this.valueName = valueName;
    }

    public static ValueEvent of(Ex ex, Value value) {
        return new ValueEvent(ex, value);
    }

    @Override
    public void write(Kryo kryo, Output output) {

    }

    @Override
    public void read(Kryo kryo, Input input) {

    }

    @Override
    public Ex getEx() {
        return ex;
    }
}
