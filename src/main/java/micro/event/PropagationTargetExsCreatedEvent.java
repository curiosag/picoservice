package micro.event;

import micro.Ex;
import micro.Hydrator;
import micro._Ex;
import micro.event.serialization.Incoming;
import micro.event.serialization.Outgoing;

import java.util.ArrayList;
import java.util.List;

public class PropagationTargetExsCreatedEvent extends ExEvent {

    public List<_Ex> targets;
    private List<Long> dehydratedTargets = new ArrayList<>();

    public PropagationTargetExsCreatedEvent(Ex ex, List<_Ex> targets) {
        super(ex);
        this.targets = targets;
    }

    public PropagationTargetExsCreatedEvent() {
        targets = new ArrayList<>();
    }

    @Override
    public void write(Outgoing output) {
        super.write(output);
        output.writeVarInt(targets.size(), true);
        targets.forEach(t -> output.writeVarLong(t.getId(), true));
    }

    @Override
    public void read(Incoming input) {
        super.read(input);
        int n = input.readVarInt(true);
        for (int i = 0; i < n; i++) {
            dehydratedTargets.add(input.readVarLong(true));
        }
    }

    @Override
    public void hydrate(Hydrator h) {
        super.hydrate(h);
        dehydratedTargets.forEach(t -> {
            targets.add(h.getExForId(t));
        });
    }

    @Override
    public void dehydrate() {
        super.dehydrate();
        dehydratedTargets.clear();
        targets.forEach(t -> dehydratedTargets.add(t.getId()));
        targets.clear();
    }
}
