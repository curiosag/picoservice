package micro.If;

import micro.*;

import java.util.HashSet;
import java.util.Set;

import static micro.PropagationType.FALSE_BRANCH;
import static micro.PropagationType.TRUE_BRANCH;

public class ExIf extends Ex {
    private Boolean condition;

    private final Set<ValuePropagation> stashed = new HashSet<>();

    ExIf(Node node, long id, F template, _Ex returnTo) {
        super(node, id, template, returnTo);
    }

    @Override
    public void processValueDownstream(Value v) {
        Check.preCondition(isLegitDownstreamValue(v));

        if (Names.condition.equals(v.getName())) {
            Check.preCondition(condition == null);
            Check.invariant(v.get() instanceof Boolean, "condition value must be boolean");
            this.condition = (Boolean) v.get();
            propagateStashedValues();
        } else {
            if (condition == null) {
                stashValuePropagations(v);
            }
            if (!Names.condition.equals(v.getName())) {
                propagateValueOnCondition(v);
            }
        }
    }

    private void propagateValueOnCondition(Value v) {
        getPropagations(v.getName()).stream()
                .filter(p1 -> !isConditioned(p1) || canPerformConditionalPropagation(p1.getPropagationType()))
                .forEach(p -> p.getTo().receive(new Value(p.getNameToPropagate(), v.get(), this)));
    }

    private void propagateStashedValues() {
        stashed.stream()
                .filter(p1 -> canPerformConditionalPropagation(p1.propagation.getPropagationType()))
                .forEach(p -> p.propagation.getTo().receive(new Value(p.propagation.getNameToPropagate(), p.value.get(), this)));
    }

    private void stashValuePropagations(Value v) {
        getPropagations(v.getName()).stream()
                .filter(this::isConditioned)
                .forEach(p -> stashed.add(new ValuePropagation(v, p)));
    }

    private boolean isConditioned(ExPropagation p) {
        return p.getPropagationType().in(FALSE_BRANCH, TRUE_BRANCH);
    }

    private boolean canPerformConditionalPropagation(PropagationType pType) {
        return condition != null && ((condition && pType == TRUE_BRANCH) || (!condition && pType == FALSE_BRANCH));
    }


}
