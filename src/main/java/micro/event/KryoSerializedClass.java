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
    InitialExecutionCreatedEvent(0, ExecutionCreatedEvent::new),
    ExecutionCreatedEvent(1, ExecutionCreatedEvent::new),

    ValueReceivedEvent(2, ValueReceivedEvent::new),
    ValueEnqueuedEvent(3, ValueEnqueuedEvent::new),
    PropagationTargetsAllocatedEvent(4, PropagationTargetsAllocatedEvent::new),
    ValueProcessedEvent(5, ValueProcessedEvent::new),
    ExDoneEvent(6, ExDoneEvent::new);

    static Map<Integer, KryoSerializedClass> idToSerializedClass = new HashMap<>();
    static Map<Class, KryoSerializedClass> classToSerializedClass = new HashMap<>();

    public final int id;
    public final Supplier<KryoSerializable> createEvent;

    KryoSerializedClass(int id, Supplier<KryoSerializable> createEvent) {
        this.id = id;
        this.createEvent = createEvent;
    }

    static void writeObject(Kryo kryo, Output output, KryoSerializable s) {
        KryoSerializedClass c = of(s.getClass());
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
        return Check.notNull(idToSerializedClass.get(id));
    }

    public static KryoSerializedClass of(Class c) {
        KryoSerializedClass result = classToSerializedClass.get(c);
        if(result == null)
        {
            throw new IllegalArgumentException();
        }
        return Check.notNull(result);
    }

    static {
        idToSerializedClass.put(0, InitialExecutionCreatedEvent);
        idToSerializedClass.put(1, ExecutionCreatedEvent);

        idToSerializedClass.put(3, ValueReceivedEvent);
        idToSerializedClass.put(4, ValueEnqueuedEvent);
        idToSerializedClass.put(5, PropagationTargetsAllocatedEvent);
        idToSerializedClass.put(6, ValueProcessedEvent);
        idToSerializedClass.put(7, ExDoneEvent);

        classToSerializedClass.put(InitialExecutionCreatedEvent.class, InitialExecutionCreatedEvent);
        classToSerializedClass.put(ExecutionCreatedEvent.class, ExecutionCreatedEvent);

        classToSerializedClass.put(ValueReceivedEvent.class, ValueReceivedEvent);
        classToSerializedClass.put(ValueEnqueuedEvent.class, ValueEnqueuedEvent);
        classToSerializedClass.put(PropagationTargetsAllocatedEvent.class, PropagationTargetsAllocatedEvent);
        classToSerializedClass.put(ValueProcessedEvent.class, ValueProcessedEvent);
        classToSerializedClass.put(ExDoneEvent.class, ExDoneEvent);
    }

}
