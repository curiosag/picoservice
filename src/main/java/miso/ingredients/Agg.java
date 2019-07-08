package miso.ingredients;

import miso.Actress;
import miso.message.Message;
import miso.message.Name;

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

        Optional<String> symbol = message.maybe(Name.symbol).map(String::valueOf);
        Optional<Object> value = message.maybe(Name.value);
        if (symbol.isPresent() && value.isPresent() && paramsRequired.contains(symbol.get())){
            paramsReceived.put(symbol.get(), value.get());
        }

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
        Message result = new Message();
        result.params.putAll(paramsReceived);
        return result;
    }

}
