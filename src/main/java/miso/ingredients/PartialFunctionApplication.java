package miso.ingredients;

import miso.ingredients.guards.Guards;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static miso.ingredients.Actresses.start;
import static miso.ingredients.guards.Guards.notEmpty;

public class PartialFunctionApplication<T> extends FunctionSignature<T> {

    final List<String> partialAppParams = new ArrayList<>();
    // Map<ExecutionId, stack of preset values of partial function applications>
    public final Map<FunctionCallLevel, Map<String, Object>> partialAppValues = new ConcurrentHashMap<>();

    protected PartialFunctionApplication(Function<T> body, List<String> partialAppParams) {
        super(body);
        this.partialAppParams.addAll(partialAppParams);
    }

    public static <T> PartialFunctionApplication<T> partialApplication(Function<T> body, String key) {
        return partialApplication(body, Collections.singletonList(key));
    }

    public static <T> PartialFunctionApplication<T> partialApplication(Function<T> body, List<String> keys) {
        PartialFunctionApplication<T> result = new PartialFunctionApplication<>(body, keys);
        start(result);
        return result;
    }

    @Override
    public void checkSanityOnStop() {
        super.checkSanityOnStop();
        if (!partialAppValues.isEmpty()) {
            throw new IllegalStateException();
        }
    }

    @Override
    protected boolean isPartialAppParam(Message m) {
        return partialAppParams.contains(m.key);
    }

    protected void setPartialAppParamValue(Message m) {
        if (isPartialAppParam(m)) {
            Map<String, Object> partials = partialAppValues.computeIfAbsent(m.origin.functionCallLevel(), k -> new HashMap<>());
            debug(m, m.origin, String.format(" << addPartialAppParamValue (%d) << ", partials.size()));
            partials.put(m.key, m.value);
        }
    }

    @Override
    protected void forwardPartialAppParamValues(FunctionSignatureState s) {
        Guards.isFalse(s.partialApplicationValuesForwarded);

        Map<String, Object> values = getPartialAppValues(s.origin);
        Origin o = s.origin.sender(this);

        values.forEach((key, value) -> super.process(Message.message(key, value, o)));
        // TODO: it is possible to make the forwarding of partially applied values not dependent on the order of messages
        // i.e. that the partially applied values have to be supplied completely before any other parameters
        s.setPartialApplicationValuesForwarded(true);

    }

    public Map<String, Object> getPartialAppValues(Origin o) {

        List<Map.Entry<FunctionCallLevel, Map<String, Object>>> matches = partialAppValues.entrySet().stream()
                .filter(e -> o.functionCallLevel().matchString.startsWith(e.getKey().matchString))
                .sorted(Comparator.comparing(i -> i.getKey().getCallStack().size()))
                .collect(Collectors.toList());

        Guards.notEmpty(matches);

        return notEmpty(matches.get(matches.size() - 1).getValue());
    }

    @Override
    public void removePartialAppValues(Origin o) {
         partialAppValues.remove(o.functionCallLevel());
    }


}
