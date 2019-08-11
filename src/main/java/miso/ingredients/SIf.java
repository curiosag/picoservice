package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.*;
import java.util.stream.Collectors;

public class SIf<T> extends Func<T> {
    public final Func<Boolean> cond;

    private class Propaganda {
        final String keyReceived;
        final String keyToPropagate;
        final Func target;

        Propaganda(String keyReceived, String keyToPropagate, Func target) {
            this.keyReceived = keyReceived;
            this.keyToPropagate = keyToPropagate;
            this.target = target;
        }
    }

    private List<Propaganda> propagateOnTrue = new ArrayList<>();
    private List<Propaganda> propagateOnFalse = new ArrayList<>();

    private List<Message> toPropagate = new ArrayList<>();

    private SIf(Func<Boolean> f) {
        f.addTarget(Name.decision, this);
        this.cond = f;
    }

    public static SIf<Integer> condInt(Func<Boolean> f) {
        return new SIf<>(f);
    }

    public void propagateOnTrue(String keyReceived, String keyToPropagate, Func target) {
        propagateOnTrue.add(new Propaganda(keyReceived, keyToPropagate, target));
    }

    public void propagateOnFalse(String keyReceived, String keyToPropagate, Func target) {
        propagateOnFalse.add(new Propaganda(keyReceived, keyToPropagate, target));
    }

    @Override
    protected void process(Message m) {
        if (m.hasKey(Name.onTrue) || m.hasKey(Name.onFalse)) {
            targets.forEach((key, target) -> send(target, new Message(key, m.value, this.address, m.opId)));
            return;
        }

        if (m.hasKey(Name.decision)) {
            propagate(m.opId, (Boolean) m.value ? propagateOnTrue : propagateOnFalse);
            return;
        }

        toPropagate.add(m);
    }

    private void propagate(OpId opId, List<Propaganda> propaganda) {
        List<Message> todo = toPropagate.stream()
                .peek(m -> System.out.println("dbg " + m.toString()))
                .filter(m -> m.opId.equals(opId)) //TODO: sender?
                .collect(Collectors.toList());

        for (Message m : todo) {
            propagate(m, propaganda);
            toPropagate.remove(m);
        }

    }

    private void propagate(Message m, List<Propaganda> propaganda) {
        for (Propaganda p : propaganda) {
            if (m.hasKey(p.keyReceived)) {
                p.target.recieve(newMsg(p.keyToPropagate, m.value, m.opId));
            }
        }
    }

}
