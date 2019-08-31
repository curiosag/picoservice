package miso.ingredients;

public class Message {
    public final Object value;
    public final String key;
    public final Origin origin;

    private Message(String key, Object value, Origin origin) {
        this.origin = origin;
        this.key = key;
        this.value = value;
    }

    public static Message message(String key, Object value, Origin origin) {
        return new Message(key, value, origin);
    }

    public Message origin(Origin o)
    {
        return message(key, value, o);
    }

    public Message sender(Function sender) {
        return new Message(key, value, origin.sender(sender));
    }


    public boolean hasKey(String value)
    {
        return key.equals(value);
    };

    @Override
    public String toString() {
        return " " + key + ":" + (value == null ? "NULL" : value.toString()) + " " + origin.toString();
    }

}
