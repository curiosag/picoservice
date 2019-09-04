package miso.ingredients.trace;

import miso.ingredients.*;

public class TraceMessage extends Message {



    private TraceMessage(Message m, Actress receiver) {
        super(Name.senderReceiverTuple, Tuple.of(m, receiver), m.origin);
    }

    public static TraceMessage traced(Message m, Actress receiver){
        return new TraceMessage(m, receiver);
    };

    private Tuple<Message, Actress> getValue(){
        return (Tuple<Message, Actress>) value;
    }

    public Message traced(){
        return getValue().left;
    }

    public Origin sender(){
        return traced().origin;
    }

    public Actress receiver(){
        return getValue().right;
    }

}
