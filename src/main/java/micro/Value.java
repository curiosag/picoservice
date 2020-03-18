package micro;

public class Value implements Persistable {
    public static Value PING = new Value(Names.ping, null, null);

    private final _Ex sender;
    private final String name;
    private final Object value;

    public Value(String name, Object value, _Ex sender) {
        this.name = name;
        this.value = value;
        this.sender = sender;
    }

    public String getName() {
        return name;
    }

    public _Ex getSender() {
        return sender;
    }

    public Object get() {
        return value;
    }

    @Override
    public void store(Persistence p) {

    }

    @Override
    public void load(Persistence p) {

    }

    public static Value of(String name, Object value, _Ex sender) {
        return new Value(name, value, sender);
    }

    @Override
    public String toString() {
        return "Value{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    public Value withSender(Ex sender)
    {
        return new Value(name, value, sender);
    }
}
