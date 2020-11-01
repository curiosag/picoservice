package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;
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
        return "Value{" + "name='" + name + ", value=" + value + '}';
    }

    public Value withSender(Ex sender) {
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

    private final static int INT = 0;
    private final static int BOOL = 1;
    private final static int LIST = 2;
    private final static int PARTIALLY_APPLIED_F = 3;

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeVarLong(senderId, true);
        output.writeString(name);

        if (value instanceof Integer) {
            output.writeVarInt(INT, true);
            output.writeVarInt((Integer) value, false);

        } else if (value instanceof Boolean) {
            output.writeVarInt(BOOL, true);
            output.writeBoolean((Boolean) value);

        } else if (value instanceof List) {
            output.writeVarInt(LIST, true);
            List<Integer> items = (List<Integer>) value;
            output.writeVarInt(items.size(), true);
            items.forEach(i -> output.writeVarInt(i, true));
        } else if (value instanceof PartiallyAppliedFunction) {
            output.writeVarInt(PARTIALLY_APPLIED_F, true);
            ((PartiallyAppliedFunction) value).write(kryo, output);
        } else {
            throw new IllegalArgumentException("value type not handled: " + (value == null ? "NULL" : value.getClass().getSimpleName()));
        }
    }

    @Override
    public void read(Kryo kryo, Input input) {
        senderId = input.readVarLong(true);
        name = input.readString();

        int valType = input.readVarInt(true);
        switch (valType) {
            case INT:
                value = input.readVarInt(false);
                break;
            case BOOL:
                value = input.readBoolean();
                break;
            case LIST:
                ArrayList<Integer> list = new ArrayList<>();
                int size = input.readVarInt(true);
                for (int i = 0; i < size; i++) {
                    list.add(input.readVarInt(true));
                }
                value = list;
                break;
            case PARTIALLY_APPLIED_F:
                PartiallyAppliedFunction p = new PartiallyAppliedFunction();
                p.read(kryo, input);
                value = p;
                break;
            default:
                throw new IllegalArgumentException("value type not handled " + valType);

        }

    }

    @Override
    public void hydrate(Hydrator h) {
        sender = h.getExForId(senderId);
        if (value instanceof PartiallyAppliedFunction) {
            ((PartiallyAppliedFunction) value).hydrate(h);
        }
    }
}
