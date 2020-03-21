package micro.exevent;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Ex;
import micro.Value;

public class ValueEvent implements ExEvent {
    private final Ex ex;
    public final Value value;

    public ValueEvent(Ex ex, Value value) {
        this.ex = ex;
        this.value = value;
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
