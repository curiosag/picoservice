package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.HashMap;
import java.util.Map;

public class If<T> extends Func<T> {

    private class Params {
        T onTrue;
        T onFalse;
        Boolean decision;
        boolean processed;
    }

    public final Func<Boolean> cond;

    private Map<OpId, Params> params = new HashMap<>();

    private Params getParams(OpId opId) {
        Params result = params.get(opId);
        if (result == null) {
            result = new Params();
            params.put(opId, result);
        }
        return result;
    }

    private void clearParams(OpId opId) {
        params.remove(opId);
    }

    private If(Func<Boolean> cond) {
        this.cond = cond;
        cond.addTarget(Name.decision, this);
        paramsRequired(Name.decision, Name.onTrue, Name.onFalse);
    }

    public static If<Integer> condInt(Func<Boolean> cond) {
        return new If<>(cond);
    }

    @Override
    protected void process(Message m) {
        Params p = getParams(m.opId);

        if (p.decision == null && m.hasKey(Name.decision)) {
            p.decision = (Boolean) m.value;
        }

        if (m.hasKey(Name.onTrue)) {
            p.onTrue = (T) m.value;
        }

        if (m.hasKey(Name.onFalse)) {
            p.onFalse = (T) m.value;
        }

        if (p.decision != null && !p.processed) {
            if (p.decision) {
                if (p.onTrue != null) {
                    send(p.onTrue, m.opId);
                    p.processed = true;
                }
            } else {
                if (p.onFalse != null) {
                    send(p.onFalse, m.opId);
                    p.processed = true;
                }
            }
        }
        if (p.decision != null && p.onFalse != null && p.onTrue != null) {
            params.remove(m.opId);
        }
    }


}
