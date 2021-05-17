package micro.If;

import micro.*;
import micro.event.KarmaEvent;
import micro.event.KarmaEventCanPropagatePendingValues;
import micro.event.ValueEnqueuedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static micro.PropagationType.FALSE_BRANCH;
import static micro.PropagationType.TRUE_BRANCH;

public class ExIf extends Ex {

    private Boolean condition;

    private final List<ValuePropagation> stash = new ArrayList<>();

    ExIf(Node node, long id, F template, _Ex returnTo) {
        super(node, id, template, returnTo);
    }

    @Override
    protected void handleKarma(KarmaEvent k) {
        Check.condition(k instanceof KarmaEventCanPropagatePendingValues);
        propagateStashedValues();
    }

    @Override
    protected Optional<KarmaEvent> getAfterlife(ValueEnqueuedEvent v) {
        if (Names.condition.equals(v.value.getName())) {
            return Optional.of(new KarmaEventCanPropagatePendingValues(this));
        }
        return Optional.empty();
    }

    @Override
    public void processValueDownstream(Value v) {
        Check.preCondition(isLegitDownstreamValue(v));

        if (Names.condition.equals(v.getName())) {
            Check.preCondition(condition == null);
            Check.invariant(v.get() instanceof Boolean, "condition value must be boolean");
            this.condition = (Boolean) v.get();
        } else {
            if (condition == null) {
                stash(v);
            }
            propagateConditionally(v);
        }
    }

    private void propagateConditionally(Value v) {
        getPropagations(v.getName()).stream()
                .filter(p1 -> !isConditioned(p1) || servesBranchChosen(p1.getPropagationType()))
                .forEach(p -> deliver(new Value(p.getNameToPropagate(), v.get(), this), p.getTo()));
    }

    private void propagateStashedValues() {
        Check.preCondition(condition != null);

        stash.stream()
                .filter(p1 -> servesBranchChosen(p1.propagation.getPropagationType()))
                .forEach(p -> deliver(new Value(p.propagation.getNameToPropagate(), p.value.get(), this), p.propagation.getTo()));
        stash.clear();
    }

    private void stash(Value v) {
        Check.preCondition(condition == null);

        // aaaaahhhhhh ... this hurts ... look at getPropagations()
        boolean orig = isRecovery;
        try {
            isRecovery = false;
            getPropagations(v.getName()).stream()
                    .filter(this::isConditioned)
                    .forEach(p -> stash.add(new ValuePropagation(v, p)));
        } finally {
            isRecovery = orig;
        }
    }

    private boolean isConditioned(ExPropagation p) {
        return p.getPropagationType().in(FALSE_BRANCH, TRUE_BRANCH);
    }

    private boolean servesBranchChosen(PropagationType pType) {
        return condition != null && ((condition && pType == TRUE_BRANCH) || (!condition && pType == FALSE_BRANCH));
    }


}
