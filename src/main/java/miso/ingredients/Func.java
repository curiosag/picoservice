package miso.ingredients;

import miso.Actress;
import miso.message.Message;

import java.util.*;

public abstract class Func<T> extends Actress {

    protected Map<String, Func> targets = new HashMap<>();

    List<String> paramsRequired = new ArrayList<>();

    public List<String> getParamsRequired() {
        return paramsRequired;
    }

    void paramsRequired(List<String> params) {
        paramsRequired.clear();
        paramsRequired.addAll(params);
    }

    void paramsRequired(String... params) {
        paramsRequired(Arrays.asList(params));
    }

    public Func<T> addTarget(String resultKey, Func r) {
        targets.put(resultKey, r);
        return this;
    }

    void send(T result, OpId opId) {
        targets.forEach((key, target) -> send(target, newMsg(key, result, opId)));
    }

    protected Message newMsg(String key, Object value, OpId opId) {
        return new Message(key, value, this.address, opId);
    }

    protected Message newMsg(Message trigger) {
        return new Message(trigger.key, trigger.value, this.address, trigger.opId);
    }

}
