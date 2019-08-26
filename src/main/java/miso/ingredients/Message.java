package miso.ingredients;

public class Message {
    public final Object value;
    public final String key;
    public final Source source;

    private Message(String key, Object value, Source source) {
        this.source = source;
        this.key = key;
        this.value = value;
    }

    public static Message message(String key, Object value, Source source) {
        return new Message(key, value, source);
    }

    public boolean hasKey(String value)
    {
        return key.equals(value);
    };

    @Override
    public String toString() {
        return " " + key + ":" + (value == null ? "NULL" : value.toString()) + " " + source.toString();
    }

}
