package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.*;

public abstract class ExEvent extends Event {
    public Ex ex;
    private long exId;

    ExEvent(Ex ex) {
        super(IdType.EX.next());
        this.ex = ex;
        this.exId = ex.getId();
    }

    ExEvent() {
    }

    public void setEx(Ex ex) {
        this.ex = ex;
    }

    public Ex getEx() {
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
        _Ex _ex = h.getExForId(exId);
        Check.invariant(_ex instanceof Ex, "..?");
        ex = (Ex) _ex;
    }

    @Override
    public String toString() {
        throw new IllegalStateException();
    }

}
