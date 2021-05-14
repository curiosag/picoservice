package micro.event.serialization;

import micro.Check;
import micro.event.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public enum SerioulizedEvent {
    InitialExCreatedEvent(0, ExCreatedEvent::new),
    ExCreatedEvent(1, ExCreatedEvent::new),

    ValueReceivedEvent(2, micro.event.ValueReceivedEvent::new),
    ValueEnqueuedEvent(3, micro.event.ValueEnqueuedEvent::new),
    PropagationTargetExsCreatedEvent(4, PropagationTargetExsCreatedEvent::new),
    ValueProcessedEvent(5, micro.event.ValueProcessedEvent::new),
    ExDoneEvent(6, ExDoneEvent::new);

    static Map<Integer, SerioulizedEvent> idToSerializedClass = new HashMap<>();
    static Map<Class, SerioulizedEvent> classToSerializedClass = new HashMap<>();

    public final int id;
    public final Supplier<Serioulizable> createEvent;

    SerioulizedEvent(int id, Supplier<Serioulizable> createEvent) {
        this.id = id;
        this.createEvent = createEvent;
    }

    public static void writeObject(Outgoing output, Serioulizable s) {
        SerioulizedEvent c = of(s.getClass());
        try {
            output.writeVarInt(c.id, true);
            s.write(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Serioulizable readObject(Incoming input) {
        int classId = input.readVarInt(true);
        Serioulizable result = forId(classId).createEvent.get();
        result.read(input);
        return result;
    }

    public static SerioulizedEvent forId(int id) {
        return Check.notNull(idToSerializedClass.get(id));
    }

    public static SerioulizedEvent of(Class c) {
        SerioulizedEvent result = classToSerializedClass.get(c);
        if(result == null)
        {
            throw new IllegalArgumentException();
        }
        return Check.notNull(result);
    }

    static {
        idToSerializedClass.put(0, InitialExCreatedEvent);
        idToSerializedClass.put(1, ExCreatedEvent);

        idToSerializedClass.put(2, ValueReceivedEvent);
        idToSerializedClass.put(3, ValueEnqueuedEvent);
        idToSerializedClass.put(4, PropagationTargetExsCreatedEvent);
        idToSerializedClass.put(5, ValueProcessedEvent);
        idToSerializedClass.put(6, ExDoneEvent);

        classToSerializedClass.put(InitialExCreatedEvent.class, InitialExCreatedEvent);
        classToSerializedClass.put(ExCreatedEvent.class, ExCreatedEvent);

        classToSerializedClass.put(ValueReceivedEvent.class, ValueReceivedEvent);
        classToSerializedClass.put(ValueEnqueuedEvent.class, ValueEnqueuedEvent);
        classToSerializedClass.put(PropagationTargetExsCreatedEvent.class, PropagationTargetExsCreatedEvent);
        classToSerializedClass.put(ValueProcessedEvent.class, ValueProcessedEvent);
        classToSerializedClass.put(ExDoneEvent.class, ExDoneEvent);
    }

}
