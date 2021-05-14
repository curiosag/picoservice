package micro;

import micro.event.serialization.Incoming;
import micro.event.serialization.Outgoing;
import micro.event.serialization.Serioulizable;

import java.util.*;

public class Value implements Hydratable, Serioulizable {

    private static final int INT = 0;
    private static final int BOOL = 1;
    private static final int LIST = 2;
    private static final int PARTIALLY_APPLIED_F = 3;
    private static final int STRING = 4;
    private static final Map<Class<?>, Integer> valueTypes = new HashMap<>();
    static {
        valueTypes.put(Integer.class, INT);
        valueTypes.put(Boolean.class, BOOL);
        valueTypes.put(List.class, LIST);
        valueTypes.put(PartiallyAppliedFunction.class, PARTIALLY_APPLIED_F);
        valueTypes.put(String.class, STRING);
    }

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

    @Override
    public void write(Outgoing output) {
        output.writeVarLong(senderId, true);
        output.writeString(name);
        Integer valType = valueTypes.get(value.getClass());
        switch (valType) {
            case INT -> {
                output.writeVarInt(valType, true);
                output.writeVarInt((Integer) value, false);
            }
            case BOOL -> {
                output.writeVarInt(valType, true);
                output.writeBoolean((Boolean) value);
            }
            case STRING -> {
                output.writeVarInt(valType, true);
                output.writeString((String) value);
            }
            case LIST -> {
                output.writeVarInt(valType, true);
                List<Integer> items = (List<Integer>) value; //TODO some type preserving list verson needed
                output.writeVarInt(items.size(), true);
                items.forEach(i -> output.writeVarInt(i, true));
            }
            case PARTIALLY_APPLIED_F -> {
                output.writeVarInt(valType, true);
                ((PartiallyAppliedFunction) value).write(output);
            }
            default -> throw new IllegalArgumentException("value type not handled " + value.getClass().getSimpleName());
        }

    }

    @Override
    public void read(Incoming input) {
        senderId = input.readVarLong(true);
        name = input.readString();

        int valType = input.readVarInt(true);
        value = switch (valType) {
            case INT ->  input.readVarInt(false);
            case BOOL ->  input.readBoolean();
            case STRING ->  input.readString();
            case LIST -> {
                ArrayList<Integer> list = new ArrayList<>();
                int size = input.readVarInt(true);
                for (int i = 0; i < size; i++) {
                    list.add(input.readVarInt(true));
                }
                yield list;
            }
            case PARTIALLY_APPLIED_F -> {
                PartiallyAppliedFunction p = new PartiallyAppliedFunction();
                p.read(input);
                yield p;
            }
            default -> throw new IllegalArgumentException("value type not handled " + valType);
        };

    }

    @Override
    public void hydrate(Hydrator h) {
        // rather no hydration of sender, since it immediately gets evicted on termination
        if (value instanceof Hydratable) {
            ((Hydratable) value).hydrate(h);
        }
    }

    @Override
    public void dehydrate() {
        if(value instanceof Hydratable) {
            ((Hydratable) value).dehydrate();
        }
    }
}
