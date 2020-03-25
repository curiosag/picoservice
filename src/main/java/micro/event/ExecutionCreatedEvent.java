package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.*;

import java.util.Objects;

public class ExecutionCreatedEvent extends NodeEvent {
    public long exIdToReturnTo;
    public long exId;
    public long templateId;

    public ExecutionCreatedEvent(){}

    public ExecutionCreatedEvent(long templateId, long exId, long exIdToReturnTo){
        this.templateId = templateId;
        this.exId = exId;
        this.exIdToReturnTo = exIdToReturnTo;
    }

    public ExecutionCreatedEvent(_Ex ex) {
        super(0);
        this.templateId = ex.getTemplate().getId();
        this.exId = ex.getId();
        this.exIdToReturnTo = ex.returnTo().getId();
    }

    @Override
    public void hydrate(Hydrator h) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionCreatedEvent that = (ExecutionCreatedEvent) o;
        return exIdToReturnTo == that.exIdToReturnTo &&
                exId == that.exId &&
                templateId == that.templateId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(exIdToReturnTo, exId, templateId);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeVarLong(templateId, true);
        output.writeVarLong(exId, true);
        output.writeVarLong(exIdToReturnTo, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        templateId = input.readVarLong(true);
        exId = input.readVarLong(true);
        exIdToReturnTo = input.readVarLong(true);
    }
}
