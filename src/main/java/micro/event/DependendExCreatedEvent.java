package micro.event;

import micro.Ex;
import micro.Hydrator;
import micro.event.serialization.Incoming;
import micro.event.serialization.Outgoing;

public class DependendExCreatedEvent extends ExEvent {

    public Long dependingOnId;

    public DependendExCreatedEvent(Ex ex, Ex dependingOn) {
        super(ex);
        this.dependingOnId = dependingOn.getId();
    }

    public DependendExCreatedEvent() {
    }

    @Override
    public void write(Outgoing out) {
        super.write(out);
        out.writeVarLong(dependingOnId, true);
    }

    @Override
    public void read(Incoming in) {
        super.read(in);
        dependingOnId = in.readVarLong(true);
    }

    @Override
    public void hydrate(Hydrator h) {
        throw new IllegalStateException("not supposed to get hydrated this way");
    }
}
