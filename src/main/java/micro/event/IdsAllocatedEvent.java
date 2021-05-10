package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Ex;
import nano.ingredients.tuples.Tuple;

public class IdsAllocatedEvent extends ExEvent {
    public Long rangeFrom;
    public Long rangeTo;

    public IdsAllocatedEvent(Ex ex, Long rangeFrom, Long rangeTo) {
        super(ex);
        this.rangeFrom = rangeFrom;
        this.rangeTo = rangeTo;
    }

    public IdsAllocatedEvent(Ex ex, Tuple<Long, Long> range){
        this(ex, range.left, range.right);
    }

    public IdsAllocatedEvent() {
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeVarLong(rangeFrom, true);
        output.writeVarLong(rangeTo, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        rangeFrom = input.readVarLong(true);
        rangeTo = input.readVarLong(true);
    }
}
