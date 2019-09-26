package miso.ingredients;

import java.util.Arrays;

public class Message {
    public final String id;
    public final Object value;
    public final String key;
    public final Origin origin;
    public Acknowledge ack = Acknowledge.N;

    protected Message(String key, Object value, Origin origin) {
        this.origin = origin;
        this.key = key;
        this.value = value;
        this.id = origin.sender.getNextMessageId();
    }

    public static Message message(String key, Object value, Origin origin) {
        return new Message(key, value, origin);
    }

    public Message origin(Origin o)
    {
        return message(key, value, o);
    }

    public Message ack(Acknowledge a){
        ack = a;
        return this;
    }

    public Message usingKey(String key)
    {
        return new Message(key, value, origin);
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
        return " " + key + ":" + (value == null ? "NULL" : value.toString());
    }

}
