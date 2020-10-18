package micro.If;

import micro.*;

import java.util.ArrayList;
import java.util.List;

import static micro.PropagationType.FALSE_BRANCH;
import static micro.PropagationType.TRUE_BRANCH;

public class ExIf extends Ex {
    private Boolean condition;

    // conditional propagations here don't protect against double propagations
    // its the business of the recipients to guard against that
    private List<ValuePropagation> toPropagateOnConditionSet = new ArrayList<>();

    ExIf(Node node, F template, _Ex returnTo) {
        super(node, template, returnTo);
    }

    @Override
    protected void alterStateFor(Value v) {
        super.alterStateFor(v);

        if (Names.condition.equals(v.getName())) {
            Check.invariant(v.get() instanceof Boolean, "condition value must be boolean");
            this.condition = (Boolean) v.get();
        }
        if (condition == null) {
            template.getPropagations(v.getName()).stream()
                    .filter(this::isConditioned)
                    .forEach(p -> toPropagateOnConditionSet.add(new ValuePropagation(v, p)));
        }
    }

    @Override
    public void processInputValue(Value v) {
        Check.isFunctionInputValue(v);

        if (Names.condition.equals(v.getName())) {
            toPropagateOnConditionSet.stream()
                    .filter(p -> canPerformConditionalPropagation(p.propagation.propagationType))
                    .forEach(p -> enQ(newPropagateValueEvent(p.value, p.propagation)));
        } else {
            template.getPropagations(v.getName()).stream()
                    .filter(p -> !isConditioned(p) || canPerformConditionalPropagation(p.propagationType))
                    .forEach(p -> enQ(newPropagateValueEvent(v, p)));
        }
    }

    private boolean isConditioned(FPropagation p) {
        return p.propagationType.in(FALSE_BRANCH, TRUE_BRANCH);
    }

    private boolean canPerformConditionalPropagation(PropagationType pType) {
        return condition != null && ((condition && pType == TRUE_BRANCH) || (!condition && pType == FALSE_BRANCH));
    }

    @Override
    public String toString() {
        return "{\"ExIf\":{" +
                "\"id\":" + getId() +
                ", \"condition\":" + condition +
                ", \"toPropagateOnConditionSet\":" + toPropagateOnConditionSet +
                ", \"template\":" + template.getId() +
                ", \"returnTo\":" + returnTo.getId() +
                "}}";
    }
}
