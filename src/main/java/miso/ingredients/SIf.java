package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.*;
import java.util.stream.Collectors;

public class SIf<T> extends Func<T> {
    public final Func<Boolean> cond;

    class StateSIf extends State {
        List<Message> pendingForPropagation = new ArrayList<>();
        Func<?> branchChosen;
        Map<String, String> branchKeyMap;
        Object result;

        StateSIf(Source source) {
            super(source);
        }
    }

    @Override
    SIf.StateSIf newState(Source source) {
        return new SIf.StateSIf(source);
    }

    @Override
    List<String> keysExpected() {
        return Collections.emptyList();
    }

    private SIf(Func<Boolean> f) {
        f.returnTo(Name.decision, this);
        cond = f;
    }

    public static SIf<Integer> condInt(Func<Boolean> f) {
        return new SIf<>(f);
    }

    @Override
    protected void process(Message m) {
        SIf.StateSIf state = (SIf.StateSIf) getState(m.source);

        if (m.hasKey(Name.onTrue) || m.hasKey(Name.onFalse)) {
            state.result = m.value;
            returnResult((T) state.result, m.source);
            removeState(m.source);
            return;
        }

        if (m.hasKey(Name.decision)) {
            Map.Entry<Func<?>, Map<String, String>> propagations = getBranch((Boolean) m.value ? Name.onTrue : Name.onFalse);
            state.branchChosen = propagations.getKey();
            state.branchKeyMap = propagations.getValue();

            state.pendingForPropagation.forEach(p -> propagate(m, state.branchKeyMap, state.branchChosen));
            return;
        }

        if (state.branchChosen == null) {
            state.pendingForPropagation.add(m);
        } else {
            propagate(m, state.branchKeyMap, state.branchChosen);
        }

    }

    private Map.Entry<Func<?>, Map<String, String>> getBranch(String returnKey) {
        List<Map.Entry<Func<?>, Map<String, String>>> result = propagations.entrySet().stream()
                .filter(e -> returnKey.equals(e.getKey().returnKey))
                .collect(Collectors.toList());

        if (result.size() != 1) {
            throw new IllegalStateException();
        }
        return result.get(0);
    }

}
