package miso.ingredients;

import miso.Actress;
import miso.Message;

import java.util.*;

public class Agg extends Actress {

    protected Map<String, Object> paramsReceived = new HashMap<>();

    public static Agg agg(String... expected) {
        Agg result = new Agg();
        result.paramsRequired(expected);
        return result;
    }

    public static Agg agg(Actress actress) {
        Agg result = new Agg();
        result.paramsRequired(actress.paramsRequired);
        result.resultTo(actress);
        return result;
    }

    @Override
    public void recieve(Message message) {
        System.out.println(this.getClass().getSimpleName() + " aggregating \n" + message.toString());

        paramsRequired.stream()
                .filter(message::hasKey)
                .forEach(e -> paramsReceived.put(e, message.get(e)));

        if (paramsReceived.size() == paramsRequired.size()) {
            super.recieve(toMessage());
            paramsReceived.clear();
        }
    }

    @Override
    protected Message getNext() {
        return toMessage();
    }

    private Message toMessage() {
        Message result = new Message(this);
        result.params.putAll(paramsReceived);
        return result;
    }

}
