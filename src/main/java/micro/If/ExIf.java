package micro.If;

import micro.*;
import micro.event.ExEvent;
import micro.event.PropagateValueEvent;
import micro.event.ValueReceivedEvent;

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
    private Set<ValuePropagation> conditionalValuePropagations = new HashSet<>();

    ExIf(Node node, F template, _Ex returnTo) {
        super(node, template, returnTo);
    }

    public ExIf(Node node) {
        super(node);
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
            } else {
                getPropagations(v.getName()).forEach(p -> { //todo unnecessary foreach, could be replaced by structure
                    if (p.getPropagationType().in(FALSE_BRANCH, TRUE_BRANCH)) {
                        conditionalValuePropagations.add(new ValuePropagation(v, p));
                    }
                });
            }
        }

        if (e instanceof PropagateValueEvent) {
            conditionalValuePropagations.removeIf(i -> i.propagation.getTo().equals(((PropagateValueEvent) e).to) && i.value.equals(((PropagateValueEvent) e).value));
        }
    }

    @Override
    public void perfromValueReceived(Value v) {
        Check.isFunctionInputValue(v);
        propagate(v);
    }

    private void propagate(Value v) {
        getPropagations(v.getName()).stream()
                .filter(this::isUnconditionalPropagation)
                .forEach(p -> raise(new PropagateValueEvent(node.getNextObjectId(), this, p.getTo(), new Value(p.getNameToPropagate(), v.get(), this))));

        if (condition != null) {
            List<ValuePropagation> canPropagate = conditionalValuePropagations.stream()
                    .filter(this::canPerformConditionalValuePropagation)
                    .collect(Collectors.toList());
            canPropagate.forEach(p -> {
                raise(new PropagateValueEvent(node.getNextObjectId(), this, p.propagation.getTo(),
                        new Value(p.propagation.getNameToPropagate(), p.value.get(), this)));
                conditionalValuePropagations.remove(p);
            });
        }
    }

    private boolean isUnconditionalPropagation(ExPropagation p) {
        return !p.getPropagationType().in(FALSE_BRANCH, TRUE_BRANCH);
    }

    private boolean canPerformConditionalValuePropagation(ValuePropagation valuePropagation) {
        switch (valuePropagation.propagation.getPropagationType()) {

            case TRUE_BRANCH:
                return condition;

            case FALSE_BRANCH:
                return !condition;

            default:
                Check.fail("invalid case " + valuePropagation.propagation.getPropagationType().name());
                return false;
        }
    }

}
