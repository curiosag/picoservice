package micro.event;

import micro.Ex;
import micro.Hydrator;
import micro._Ex;
import micro._F;
import micro.event.serialization.Incoming;
import micro.event.serialization.Outgoing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PropagationTargetExsCreatedEvent extends ExEvent {

    public List<_Ex> targets;
    public List<_F> targetTemplates;

    private final List<Long> dehydratedTargets = new ArrayList<>();

    public PropagationTargetExsCreatedEvent(Ex ex, List<_Ex> targets) {
        super(ex);
        this.targets = targets;
        targetTemplates = mapTemplates(targets);
    }

    private List<_F> mapTemplates(List<_Ex> targets) {
        return targets.stream().map(_Ex::getTemplate).collect(Collectors.toList());
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
        targetTemplates = mapTemplates(targets);
    }

    @Override
    public void dehydrate() {
        super.dehydrate();
        dehydratedTargets.clear();
        targets.forEach(t -> dehydratedTargets.add(t.getId()));
        targets.clear();
    }
}
