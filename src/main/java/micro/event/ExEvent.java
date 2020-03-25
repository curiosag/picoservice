package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Check;
import micro.Hydrator;
import micro._Ex;

public abstract class ExEvent extends Event {
    public _Ex ex;
    long exId;

    public ExEvent(long id, _Ex ex) {
        super(id);
        this.ex = ex;
        this.exId = ex.getId();
    }

    protected ExEvent() {
    }

    public void setEx(_Ex ex) {
        this.ex = ex;
    }

    public _Ex getEx() {
        Check.notNull(ex);
        return ex;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeVarLong(exId, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        exId = input.readVarLong(true);
    }

    @Override
    public void hydrate(Hydrator h) {
        ex = h.getExForId(exId);
    }
}
