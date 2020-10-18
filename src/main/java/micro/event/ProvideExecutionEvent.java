package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Ex;
import micro.Hydrator;
import micro._F;

public class ProvideExecutionEvent extends ExEvent {
    private long targetId;
    public _F target;

    public ProvideExecutionEvent(Ex ex, _F target) {
        super(ex);
        this.target = target;
    }

    public ProvideExecutionEvent() {

    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeVarLong(targetId, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        targetId = input.readVarLong(true);
    }

    @Override
    public void hydrate(Hydrator h) {
        super.hydrate(h);
        target = h.getFForId(targetId);
    }
}
