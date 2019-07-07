package miso.ingredients;

import miso.Message;
import miso.Name;

public class If<T> extends Func {

    private String keyDecision = Name.decision;
    private String keyOnTrue = Name.onTrue;
    private String keyOnFalse = Name.onFalse;


    public If<T> paramKeys(String keyDecision, String keyOnTrue, String keyOnFalse) {
        this.keyDecision = keyDecision;
        this.keyOnTrue = keyOnTrue;
        this.keyOnFalse = keyOnFalse;
        await(keyDecision, keyOnTrue, keyOnFalse);
        return this;
    }

    private If() {
        paramKeys(keyDecision, keyOnTrue, keyOnFalse);
    }

    public static If branch() {
        return new If();
    }

    @Override
    protected Message getNext() {
        return getCurrent()
                .map(m -> {
                    Boolean decision = (Boolean) m.get(keyDecision);
                    return decision ? message(m.get(keyOnTrue)) : message(m.get(keyOnFalse));
                })
                .orElseGet(NULL);
    }

    private Message message(Object result) {
        return Message.of(this, resultKey, result);
    }

}
