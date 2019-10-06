package nano.ingredients;

import java.io.Serializable;
import java.util.Arrays;


public class Message implements Serializable {
    private static final long serialVersionUID = 0L;
    public final String id;
    private final Serializable value;
    public final String key;
    public final Origin origin;
    public Acknowledge ack = Acknowledge.N;
    private transient boolean isReplay;


    public boolean isReplay() {
        return isReplay;
    }

    public Message setReplay(boolean replay) {
        isReplay = replay;
        return this;
    }

    private static Long maxMessageId = 0L;

    protected Message(String key, Serializable value, Origin origin) {
        this.origin = origin;
        this.key = key;
        this.value = value;
        this.id = String.format("%04d", ++maxMessageId);
    }

    public static Message message(String key, Serializable value, Origin origin) {
        return new Message(key, value, origin);
    }

    public Message origin(Origin o)
    {
        return message(key, getValue(), o);
    }

    public Message ack(Acknowledge a){
        ack = a;
        return this;
    }

    public Message usingKey(String key)
    {
        return new Message(key, getValue(), origin);
    }


    public boolean hasKey(String value)
    {
        return key.equals(value);
    };

    public boolean hasAnyKey(String ... keys)
    {
        return Arrays.asList(keys).contains(key);
    };

    @Override
    public String toString() {
        return " " + key + ":" + (getValue() == null ? "NULL" : getValue().toString());
    }

    public Serializable getValue() {
        return value;
    }
}
