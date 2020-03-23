package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Hydrator;

public class QueueRemoveEvent extends NodeEvent {
    public long idEventToRemove;

    public QueueRemoveEvent() {
    }

    public QueueRemoveEvent(long idEventToRemove) {
        this.idEventToRemove = idEventToRemove;
    }

    @Override
    public void hydrate(Hydrator h) {

    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeVarLong(idEventToRemove, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        idEventToRemove = input.readVarLong(true);
    }
}
