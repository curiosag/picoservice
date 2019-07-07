package miso.ingredients;

import miso.Actress;
import miso.Message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Agg extends Actress {

    protected Map<String, Object> received = new HashMap<>();
    protected List<String> awaiting;


    public Agg await(String... expected){
        this.awaiting = Arrays.asList(expected);
        return this;
    }

    public static Agg agg(String... expected) {
        return new Agg().await(expected);
    }

    @Override
    public void recieve(Message message) {
        System.out.println(this.getClass().getSimpleName() + " aggregating \n" + message.toString());

        awaiting.stream()
                .filter(message::hasKey)
                .forEach(e -> received.put(e, message.get(e)));

        if (received.size() == awaiting.size()) {
            super.recieve(toMessage());
            received.clear();
        }
    }

    @Override
    protected Message getNext() {
        return toMessage();
    }

    private Message toMessage() {
        Message result = new Message(this);
        result.params.putAll(received);
        return result;
    }

}
