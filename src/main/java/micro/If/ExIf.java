package micro.If;

import micro.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static micro.PropagationType.FALSE_BRANCH;
import static micro.PropagationType.TRUE_BRANCH;

public class ExIf extends Ex {
    private Boolean condition;

    // conditional propagations here don't protect against double propagations
    // its the business of the recipients to guard against that
    private Set<ValuePropagation> toPropagateOnConditionSet = new HashSet<>();

    ExIf(Node node, long id, F template, _Ex returnTo) {
        super(node, id, template, returnTo);
    }

    @Override
    protected int getNumberCustomIdsNeeded() {
        return 0; // TODO: enough to return count of functions behind condition + behind the branch with higher function count than the other
    }

    @Override
    public void processDownstreamValue(Value v) {
        Check.preCondition(isLegitDownstreamValue(v));

        if (Names.condition.equals(v.getName())) {
            Check.invariant(v.get() instanceof Boolean, "condition value must be boolean");
            this.condition = (Boolean) v.get();
        }
        if (condition == null) {
            getPropagations(v.getName()).stream()
                    .filter(this::isConditioned)
                    .forEach(p -> toPropagateOnConditionSet.add(new ValuePropagation(v, p)));
        }

        if (Names.condition.equals(v.getName())) {
            List<ValuePropagation> sel = toPropagateOnConditionSet.stream()
                    .filter(p -> canPerformConditionalPropagation(p.propagation.getPropagationType())).collect(Collectors.toList());
            sel.forEach(p -> p.propagation.getTo().receive(new Value(p.propagation.getNameToPropagate(), p.value.get(), this)));
        } else {
            List<ExPropagation> sel = getPropagations(v.getName()).stream()
                    .filter(p -> !isConditioned(p) || canPerformConditionalPropagation(p.getPropagationType()))
                    .collect(Collectors.toList());
            sel.forEach(p -> p.getTo().receive(new Value(p.getNameToPropagate(), v.get(), this)));
        }
    }

    private boolean isConditioned(ExPropagation p) {
        return p.getPropagationType().in(FALSE_BRANCH, TRUE_BRANCH);
    }

    private boolean canPerformConditionalPropagation(PropagationType pType) {
        return condition != null && ((condition && pType == TRUE_BRANCH) || (!condition && pType == FALSE_BRANCH));
    }


}
