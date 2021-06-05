package micro.event;

import micro.Ex;
import micro.Hydrator;
import micro.event.serialization.Incoming;
import micro.event.serialization.Outgoing;

public class ExCreatedEvent extends ExEvent {

    private Long FId;

    public ExCreatedEvent(Ex ex) {
        super(ex);
        this.FId = ex.template.getId();
    }

    public ExCreatedEvent() {
    }

    public Long getFId() {
        return FId;
    }

    @Override
    public void write(Outgoing output) {
        super.write(output);
        output.writeVarLong(FId, true);
    }

    @Override
    public void read(Incoming input) {
        super.read(input);
        FId = input.readVarLong(true);
    }

    @Override
    public void hydrate(Hydrator h) {
        throw new IllegalStateException("not supposed to get hydrated this way");
    }
}
