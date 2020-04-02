package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Hydrator;

public class QueueRemoveEvent extends NodeEvent {
    public long idEventToRemove;
    public long idRelatedExecution;

    public QueueRemoveEvent() {
        super(0);
    }

    public QueueRemoveEvent(long idEventToRemove, long idRelatedExecution) {
        this.idEventToRemove = idEventToRemove;
        this.idRelatedExecution = idRelatedExecution;
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

    @Override
    public String toString() {
        return "{\"QueueRemoveEvent\":{" +
                "\"idEventToRemove\":" + idEventToRemove +
                ", \"idRelatedExecution\":" + idRelatedExecution +
                "}}";
    }
}
