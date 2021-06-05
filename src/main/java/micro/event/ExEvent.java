package micro.event;

import micro.*;
import micro.event.serialization.Incoming;
import micro.event.serialization.Outgoing;

public abstract class ExEvent extends Event {

    public String exName;
    public _Ex ex;
    public long exId;
    public long exReturnToId;

    public ExEvent(Ex ex) {
        super();
        this.ex = ex;
        this.exId = ex.getId();
        this.exReturnToId = ex.getReturnTo().getId();
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

    public long getExId() {
        return exId;
    }

    @Override
    public void write(Outgoing output) {
        super.write(output);
        output.writeVarLong(exId, true);
        output.writeVarLong(exReturnToId, true);
    }

    @Override
    public void read(Incoming input) {
        super.read(input);
        exId = input.readVarLong(true);
        exReturnToId = input.readVarLong(true);
    }

    @Override
    public void hydrate(Hydrator h) {
        ex = h.getExForId(exId);
    }

    @Override
    public void dehydrate() {
        if(ex != null) {
            exName = ((F) ex.getTemplate()).getLabel();
        }
        ex = null;
    }
}
