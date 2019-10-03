package nano.ingredients;

import nano.ingredients.guards.Guards;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static nano.ingredients.Ensemble.wire;
import static nano.ingredients.guards.Guards.notEmpty;

public class PartialFunctionApplication<T extends Serializable> extends FunctionSignature<T> {

    final List<String> partialAppParams = new ArrayList<>();
    // Map<ExecutionId, stack of preset values of partial function applications>
    public final Map<FunctionCallTreeLocation, Map<String, Serializable>> partialAppValues = new ConcurrentHashMap<>();

    protected PartialFunctionApplication(Function<T> body, List<String> partialAppParams) {
        super(body);
        this.partialAppParams.addAll(partialAppParams);
    }

    public static <T extends Serializable> PartialFunctionApplication<T> partialApplication(Function<T> body, String key) {
        return partialApplication(body, Collections.singletonList(key));
    }

    public static <T extends Serializable> PartialFunctionApplication<T> partialApplication(Function<T> body, List<String> keys) {
        PartialFunctionApplication<T> result = new PartialFunctionApplication<>(body, keys);
        wire(result);
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
    protected boolean isPartialAppParam(Message m) {
        return partialAppParams.contains(m.key);
    }

    protected void setPartialAppParamValue(Message m) {
        if (isPartialAppParam(m)) {
            Map<String, Serializable> partials = partialAppValues.computeIfAbsent(m.origin.functionCallTreeLocation(), k -> new HashMap<>());
            partials.put(m.key, m.getValue());
        }
    }

    @Override
    protected void forwardPartialAppParamValues(FunctionSignatureState s) {
        Guards.isFalse(s.partialApplicationValuesForwarded);

        Map<String, Serializable> values = getPartialAppValues(s.origin);
        Origin o = s.origin.sender(this);

        values.forEach((key, value) -> super.process(Message.message(key, value, o)));
        // TODO: it is possible to make the forwarding of partially applied values not dependent on the order of messages
        // i.e. that the partially applied values have to be supplied completely before any other parameters
        s.setPartialApplicationValuesForwarded(true);

    }

    public Map<String, Serializable> getPartialAppValues(Origin o) {

        List<Map.Entry<FunctionCallTreeLocation, Map<String, Serializable>>> matches = partialAppValues.entrySet().stream()
                .filter(e -> o.functionCallTreeLocation().getExecutionId().equals(e.getKey().getExecutionId()))
                .filter(e -> o.functionCallTreeLocation().getCallStack().startsWith(e.getKey().getCallStack()))
                .sorted(Comparator.comparing(i -> i.getKey().getCallStack().size()))
                .collect(Collectors.toList());

        Guards.notEmpty(matches);

        return notEmpty(matches.get(matches.size() - 1).getValue());
    }

    @Override
    public void removePartialAppValues(Origin o) {
        partialAppValues.remove(o.functionCallTreeLocation());
    }


}
