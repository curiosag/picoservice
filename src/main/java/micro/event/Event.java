package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Hydratable;

public abstract class Event implements Hydratable, KryoSerializable {

    Event()
    {
    }

    @Override
    public void write(Kryo kryo, Output output) {
    }

    @Override
    public void read(Kryo kryo, Input input) {

    }
}
