package nano.ingredients.trace;

import nano.ingredients.*;
import nano.ingredients.tuples.SerializableTuple;

public class TraceMessage extends Message {

    private TraceMessage(Message m, Actress receiver) {
        super(Name.senderReceiverTuple, SerializableTuple.of(m, receiver), m.origin);
    }

    public static TraceMessage traced(Message m, Actress receiver){
        return new TraceMessage(m, receiver);
    };

    private SerializableTuple<Message, Actress> getTracedValue(){
        return (SerializableTuple<Message, Actress>) getValue();
    }

    public Message traced(){
        return getTracedValue().left;
    }

    public Origin sender(){
        return traced().origin;
    }

    public Actress receiver(){
        return getTracedValue().right;
    }

}
