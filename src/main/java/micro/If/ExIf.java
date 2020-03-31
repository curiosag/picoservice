package micro.If;

import micro.*;
import micro.event.ExEvent;
import micro.event.PropagateValueEvent;
import micro.event.ValueReceivedEvent;

import java.util.HashSet;
import java.util.Set;

import static micro.PropagationType.FALSE_BRANCH;
import static micro.PropagationType.TRUE_BRANCH;

public class ExIf extends Ex {
    private Boolean condition;

    // conditional propagations here don't protect against double propagations
    // its the business of the recipients to guard against that
    private Set<ValuePropagation> toPropagateOnConditionSet = new HashSet<>();

    ExIf(Node node, F template, _Ex returnTo) {
        super(node, template, returnTo);
    }

    @Override
    public void handle(ExEvent e) {
        alterStateFor(e);
        super.handle(e); // super depends an local state changed
    }

    @Override
    public void recover(ExEvent e) {
        super.recover(e);
        alterStateFor(e);
    }

    private void alterStateFor(ExEvent e) {
        if (e instanceof ValueReceivedEvent) {
            Value v = ((ValueReceivedEvent) e).value;
            if (Names.condition.equals(v.getName())) {
                Check.invariant(v.get() instanceof Boolean, "condition value must be boolean");
                this.condition = (Boolean) v.get();
            }
            if (condition == null) {
                getPropagations(v.getName()).stream()
                        .filter(this::isConditioned)
                        .forEach(p -> toPropagateOnConditionSet.add(new ValuePropagation(v, p)));
            }
        }
    }

    @Override
    public void performValueReceived(Value v) {
        Check.isFunctionInputValue(v);

        if (Names.condition.equals(v.getName())) {
            toPropagateOnConditionSet.stream()
                    .filter(p -> canPerformConditionalPropagation(p.propagation.getPropagationType()))
                    .forEach(p -> raise(newPropagateValueEvent(p.value, p.propagation)));
        } else {
            getPropagations(v.getName()).stream()
                    .filter(p -> !isConditioned(p) || canPerformConditionalPropagation(p.getPropagationType()))
                    .forEach(p -> raise(newPropagateValueEvent(v, p)));
        }
    }

    private boolean isConditioned(ExPropagation p) {
        return p.getPropagationType().in(FALSE_BRANCH, TRUE_BRANCH);
    }

    private boolean canPerformConditionalPropagation(PropagationType pType) {
        return condition != null && ((condition && pType == TRUE_BRANCH) || (!condition && pType == FALSE_BRANCH));
    }

    private PropagateValueEvent newPropagateValueEvent(Value v, ExPropagation p) {
        return new PropagateValueEvent(node.getNextObjectId(), this, p.getTo(), new Value(p.getNameToPropagate(), v.get(), this));
    }

}
