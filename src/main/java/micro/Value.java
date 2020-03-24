package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class Value implements Hydratable, KryoSerializable {

    private long senderId;
    private _Ex sender;
    private String name;
    private Object value;

    public Value(String name, Object value, _Ex sender) {
        this.name = name;
        this.value = value;
        this.sender = sender;
        this.senderId = sender.getId();
    }

    public Value() {
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

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeVarLong(senderId, true);
        output.writeString(name);

        if (value instanceof Integer) {

            output.writeVarInt(0, true);
            output.writeVarInt((Integer) value, false);

        } else if (value instanceof Boolean) {

            output.writeVarInt(1, true);
            output.writeBoolean((Boolean) value);

        } else {
            throw new IllegalArgumentException("unknown value type " + value.getClass().getSimpleName());
        }
    }

    @Override
    public void read(Kryo kryo, Input input) {
        senderId = input.readVarLong(true);
        name = input.readString();

        int valType = input.readVarInt(true);
        switch (valType) {
            case 0:
                value = input.readVarInt(false);
                break;
            case 1:
                value = input.readBoolean();
                break;
            default:
                throw new IllegalArgumentException("unknown value type " + valType);

        }

    }

    @Override
    public void hydrate(Hydrator h) {
        sender = h.getExForId(senderId);
    }
}
