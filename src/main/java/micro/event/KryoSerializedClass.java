package micro.event;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.Check;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public enum KryoSerializedClass {
    QueueRemoveEvent(0, QueueRemoveEvent::new),
    ExecutionCreatedEvent(1, ExecutionCreatedEvent::new),
    PropagateValueEvent(5, PropagateValueEvent::new),
    ValueReceivedEvent(6, ValueReceivedEvent::new),
    ValueProcessedEvent(7, ValueProcessedEvent::new);

    static Map<Integer, KryoSerializedClass> idToSerializedClass = new HashMap<>();
    static Map<Class, KryoSerializedClass> classToSerializedClass = new HashMap<>();

    public final int id;
    public final Supplier<KryoSerializable> createEvent;

    KryoSerializedClass(int id, Supplier<KryoSerializable> createEvent) {
        this.id = id;
        this.createEvent = createEvent;
    }

    static void writeObject(Kryo kryo, Output output, KryoSerializable s) {
        KryoSerializedClass c = forClass(s.getClass());
        try {
            output.writeVarInt(c.id, true);
            s.write(kryo, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static KryoSerializable readObject(Kryo kryo, Input input) {
        int classId = input.readVarInt(true);
        KryoSerializable result = forId(classId).createEvent.get();
        result.read(kryo, input);
        return result;
    }

    public static KryoSerializedClass forId(int id) {
        try {
            return Check.notNull(idToSerializedClass.get(id));
        } catch (Exception e) {
            throw e;
        }
    }

    public static KryoSerializedClass forClass(Class c) {
        KryoSerializedClass result = classToSerializedClass.get(c);
        if(result == null)
        {
            throw new IllegalArgumentException();
        }
        return Check.notNull(result);
    }

    static {
        idToSerializedClass.put(0, QueueRemoveEvent);
        idToSerializedClass.put(1, ExecutionCreatedEvent);
        idToSerializedClass.put(5, PropagateValueEvent);
        idToSerializedClass.put(6, ValueReceivedEvent);
        idToSerializedClass.put(7, ValueProcessedEvent);

        classToSerializedClass.put(QueueRemoveEvent.class, QueueRemoveEvent);
        classToSerializedClass.put(ExecutionCreatedEvent.class, ExecutionCreatedEvent);
        classToSerializedClass.put(PropagateValueEvent.class, PropagateValueEvent);
        classToSerializedClass.put(ValueReceivedEvent.class, ValueReceivedEvent);
        classToSerializedClass.put(ValueProcessedEvent.class, ValueProcessedEvent);
    }

}
