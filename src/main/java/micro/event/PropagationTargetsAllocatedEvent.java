package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Ex;
import micro._Ex;

import java.util.ArrayList;
import java.util.List;

public class PropagationTargetsAllocatedEvent extends ExEvent {

    public List<_Ex> targets;

    public PropagationTargetsAllocatedEvent(Ex ex, List<_Ex> targets) {
        super(ex);
        this.targets = targets;
    }

    public PropagationTargetsAllocatedEvent() {
        targets = new ArrayList<>();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        //TODO
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        //TODO
    }
}
