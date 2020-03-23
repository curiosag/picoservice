package micro.event;

import micro.Ex;
import micro.Hydrator;
import micro.Value;

public class ValueProcessedEvent extends ExEvent {
    public String valueName;

    ValueProcessedEvent(){
    }

    public ValueProcessedEvent(Ex ex, String valueName) {
        super(ex);
        this.valueName = valueName;
    }

    public static ValueEvent of(Ex ex, Value value) {
        return new ValueEvent(ex, value);
    }

    @Override
    public void hydrate(Hydrator h) {

    }
}
