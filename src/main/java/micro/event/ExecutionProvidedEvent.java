package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Ex;
import micro.Hydrator;
import micro._Ex;

public class ExecutionProvidedEvent extends ExEvent {
    private long providedId;
    public _Ex provided;

    public ExecutionProvidedEvent(Ex requestedBy, _Ex provided) {
        super(requestedBy);
        this.provided = provided;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeVarLong(providedId, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        providedId = input.readVarLong(true);
    }

    @Override
    public void hydrate(Hydrator h) {
        super.hydrate(h);
        provided = h.getExForId(providedId);
    }
}
