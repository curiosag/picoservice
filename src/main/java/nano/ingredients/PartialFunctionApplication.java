package nano.ingredients;

import nano.ingredients.guards.Guards;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static nano.ingredients.Ensemble.attachActor;
import static nano.ingredients.guards.Guards.notEmpty;

public class PartialFunctionApplication<T extends Serializable> extends Function<T> {

    final List<String> partialAppParams = new ArrayList<>();
    // Map<ExecutionId, stack of preset values of partial function applications>
    public final Map<ComputationPathLocation, Map<String, Serializable>> partialAppValues = new ConcurrentHashMap<>();
    private final FunctionSignature<T> inner;

    private PartialFunctionApplication(FunctionSignature<T> inner, List<String> partialAppParams) {
        this.inner = inner;
        this.partialAppParams.addAll(partialAppParams);
    }

    public static <T extends Serializable> PartialFunctionApplication<T> partialApplication(Function<T> body, String key) {
        return partialApplication(body, Collections.singletonList(key));
    }

    public static <T extends Serializable> PartialFunctionApplication<T> partialApplication(Function<T> body, List<String> keys) {
        FunctionSignature<T> signature = new FunctionSignature<>(body);
        attachActor(signature);

        PartialFunctionApplication<T> result = new PartialFunctionApplication<>(signature, keys);
        attachActor(result);
        return result;
    }

    @Override
    public void checkSanityOnStop() {
        super.checkSanityOnStop();
        if (!partialAppValues.isEmpty()) {
            System.out.println(String.format("-->  %s %s: %d partialAppValues left after stop", address.toString(), address.toString(), partialAppValues.size()));
            partialAppValues.clear();
        }
    }

    @Override
    protected State newState(Origin origin) {
        return new PartialFunctionApplicationState(origin);
    }

    @Override
    protected boolean belongsToMe(String key) {
        return true;
    }

    @Override
    protected void processInner(Message m, State state) {
    }

    @Override
    public void process(Message m) {
        trace(m);

        if (m.key.equals(Name.removePartialAppValues)) {
            removePartialAppValues(m.origin);
            return;
        }

        if (m.key.equals(Name.result)) {
            removeState(m.origin);
            returnTo.tell(m.origin(m.origin.sender(this)));
            return;
        }

        if (forwardingPartialAppParamValues(m)) {
            inner.tell(m);
            return;
        }

        PartialFunctionApplicationState state = (PartialFunctionApplicationState) getState(m.origin);
        Map<String, Serializable> partialAppValuesForCall = partialAppValues.get(m.origin.computationPathLocation());
        if (partialAppValuesForCall != null) {
            partialAppValuesForCall.forEach((k, v) -> state.partialAppValues.put(k, v));
        } else {

            //TODO what to do with a function that only takes a partial that has all variables set?
            //TODO partials depend on the order of messages, partial values must not come last
            if (isPartialAppParam(m)) {
                Map<String, Serializable> partials = state.partialAppValues;

                if (partials.get(m.key) != null) {
                    throw new IllegalStateException();
                }

                partials.put(m.key, m.getValue());
                if (partials.size() == partialAppParams.size()) {
                    partialAppValues.put(m.origin.computationPathLocation(), partials);
                    forwardPartialAppParamValues(state);
                }
                return;
            }
        }
        if (!isDownstreamMessage(m)) {
            throw new IllegalStateException();
        }

        if (!state.partialApplicationValuesForwarded) {
            forwardPartialAppParamValues(state);
        }
        inner.tell(m);
    }

    private boolean isDownstreamMessage(Message m) {
        return (m.origin.getSender() instanceof FunctionCall);
    }

    private boolean forwardingPartialAppParamValues(Message m) {
        return m.origin.getSender().equals(this) && isPartialAppParam(m);
    }

    protected boolean isPartialAppParam(Message m) {
        return partialAppParams.contains(m.key);
    }

    protected void forwardPartialAppParamValues(PartialFunctionApplicationState s) {
        Guards.isFalse(s.partialApplicationValuesForwarded);

        Map<String, Serializable> values = getPartialAppValues(s.origin);
        values.forEach((key, value) -> tell(Message.message(key, value, s.origin.sender(this))));
        s.setPartialApplicationValuesForwarded(true);
    }

    public Map<String, Serializable> getPartialAppValues(Origin o) {
        // hopefully the longest call stack contains the most recent partial app values
        List<Map.Entry<ComputationPathLocation, Map<String, Serializable>>> matches = partialAppValues.entrySet().stream()
                .filter(e -> o.computationPathLocation().getExecutionId().equals(e.getKey().getExecutionId()))
                .filter(e -> o.computationPathLocation().getCallStack().startsWith(e.getKey().getCallStack()))
                .sorted(Comparator.comparing(i -> i.getKey().getCallStack().size()))
                .collect(Collectors.toList());

        Guards.notEmpty(matches);
        return notEmpty(matches.get(matches.size() - 1).getValue());
    }

    public void removePartialAppValues(Origin o) {
        partialAppValues.remove(o.computationPathLocation());
    }

    @Override
    public Function<T> returnTo(Function<?> f, String returnKey) {
        inner.returnTo(this, returnKey);
        return super.returnTo(f, returnKey);
    }

    @Override
    void propagate(Message m) {
        inner.propagate(m);
    }

    @Override
    public void label(String sticker) {
        inner.label(sticker);
    }

    @Override
    public PartialFunctionApplication<T> propagate(String keyReceived, String keyToPropagate, Function target) {
        inner.propagate(keyReceived, keyToPropagate, target);
        return this;
    }
}
