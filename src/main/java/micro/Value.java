package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class Value implements KryoSerializable {
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

    @Override
    public void write(Kryo kryo, Output output) {

    }

    @Override
    public void read(Kryo kryo, Input input) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value1 = (Value) o;
        return Objects.equals(sender, value1.sender) &&
                Objects.equals(name, value1.name) &&
                Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, name, value);
    }
}
