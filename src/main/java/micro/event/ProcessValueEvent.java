package micro.event;

import micro.Value;
import micro._Ex;

public class ProcessValueEvent extends ValueEvent {

    public ProcessValueEvent(){
        super();
    }

    public ProcessValueEvent(long exId, _Ex ex, Value value) {
        super(exId, ex, value);
    }

    @Override
    public String toString() {
        return "{\"ProcessValueEvent\":{" +
                "\"value\":" + value +
                ", \"ex\":" + ex.getId() +
                "}}";
    }
}
