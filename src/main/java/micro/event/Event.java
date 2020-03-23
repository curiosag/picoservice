package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Hydratable;
import micro.Id;

public abstract class Event implements Id, Hydratable, KryoSerializable {

    private long id;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeVarLong(id, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        id = input.readVarLong(true);
    }
}
