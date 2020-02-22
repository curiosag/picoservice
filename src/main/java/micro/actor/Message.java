package micro.actor;

import micro.Ex;
import micro.Value;

public class Message {
    public final Value value;
    public final Ex target;

    public Message(Value value, Ex target) {
        this.value = value;
        this.target = target;
    }

}
