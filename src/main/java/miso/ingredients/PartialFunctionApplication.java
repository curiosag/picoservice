package miso.ingredients;

import java.util.*;

import static miso.ingredients.Actresses.start;
import static miso.ingredients.Origin.origin;
import static miso.ingredients.PartialAppMatcher.matcher;
import static miso.ingredients.guards.Guards.notEmpty;
import static miso.ingredients.guards.Guards.notNull;

public class PartialFunctionApplication<T> extends FunctionSignature<T> {

    final List<String> partialAppParams = new ArrayList<>();
    // Map<ExecutionId, stack of preset values of partial function applications>
    final Map<PartialAppMatcher, Stack<Map<String, Object>>> partialAppValues = new HashMap<>();

    protected PartialFunctionApplication(Function<T> body, List<String> partialAppParams) {
        super(body);
        this.partialAppParams.addAll(partialAppParams);
    }

    public static <T> PartialFunctionApplication<T> partialApplication(Function<T> body, List<String> keys) {
        PartialFunctionApplication<T> result = new PartialFunctionApplication<>(body, keys);
        start(result);
        return result;
    }

    @Override
    protected boolean isPartialAppParamValue(Message m) {
        return partialAppParams.contains(m.key);
    }

    @Override
    protected void pushPartialAppParamValue(Message m) {
        if (isPartialAppParamValue(m)) {
            Stack<Map<String, Object>> stack = partialAppValues.computeIfAbsent(matcher(m.origin), k ->
            {
                Stack<Map<String, Object>> s = new Stack<>();
                s.push(new HashMap<>());
                return s;
            });

            stack.peek().put(m.key, m.value);
        }
    }

    @Override
    protected void forwardPartialAppParamValues(FunctionSignatureState s) {
        if (!s.partialApplicationValuesForwarded) {
            Map<String, Object> values = notEmpty(notNull(partialAppValues.get(matcher(s.caller)))).peek();
            Origin o = origin(s.origin.sender, s.origin.sender, s.origin.executionId, s.origin.callLevel, s.origin.seqNr);
            o.partiallyApplied = true;
            values.forEach((key, value) -> receive(Message.message(key, value, o)));
            // TODO: it is possible to make the forwarding of partially applied values not dependent on the order of messages
            // i.e. that the partially applied values have to be supplied completely before any other parameters
            s.setPartialApplicationValuesForwarded(true);
        }
    }

    public void popPartialApp(Origin o) {
        PartialAppMatcher matcher = matcher(o);
        notNull(partialAppValues.get(matcher));
        partialAppValues.remove(matcher);
    }


}
