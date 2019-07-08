package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

public class If<T> extends Func {

    private String keyDecision;
    private String keyOnTrue;
    private String keyOnFalse;

    public If<T> paramKeys(String keyDecision, String keyOnTrue, String keyOnFalse) {
        this.keyDecision = keyDecision;
        this.keyOnTrue = keyOnTrue;
        this.keyOnFalse = keyOnFalse;
        paramsRequired(keyDecision, keyOnTrue, keyOnFalse);
        return this;
    }

    private If() {
        paramKeys(Name.decision, Name.onTrue, Name.onFalse);
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
        return Message.of(resultKey, result);
    }

}
