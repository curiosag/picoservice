package micro.If;

import micro.*;
import micro.event.AfterlifeEvent;
import micro.event.AfterlifeEventCanPropagatePendingValues;
import micro.event.PropagationTargetExsCreatedEvent;
import micro.event.ValueEnqueuedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static micro.PropagationType.COND_FALSE_BRANCH;
import static micro.PropagationType.COND_TRUE_BRANCH;

public class ExIf extends Ex {

    private ExIfState state = ExIfState.INITIAL;
    private Boolean condition;

    private final List<Value> stash = new ArrayList<>();
    private static final boolean debugIfCount = false;

    ExIf(Env env, long id, F template, _Ex returnTo) {
        super(env, id, template, returnTo);
    }

    @Override
    protected boolean triggerPropagationTargetExsCreatedEvent(ValueEnqueuedEvent valueEvent) {
        List<FPropagation> propagations =
                switch (state) {
                    case INITIAL -> template.getPropagations().stream()
                            .filter(t -> t.propagationType == PropagationType.COND_CONDITION || t.propagationType == PropagationType.INDISCRIMINATE)
                            .collect(Collectors.toList());
                    case CALCULATING_COND -> filterBranchTargets(template.getPropagations(), valueEvent);
                    case CALCULATING_BRANCH -> Collections.emptyList();
                };

        var targets = propagations.stream().map(p -> p.target).distinct().collect(Collectors.toList());

        state = switch (state) { // a "terminated" state isn't needed as of now
            case INITIAL -> ExIfState.CALCULATING_COND;
            case CALCULATING_COND -> targets.isEmpty() ? state : ExIfState.CALCULATING_BRANCH;
            case CALCULATING_BRANCH -> state;
        };

        if (!targets.isEmpty()) {
            push(new PropagationTargetExsCreatedEvent(this, env.createTargets(this, targets)));
            return true;
        } else
            return false;
    }

    private List<FPropagation> filterBranchTargets(List<FPropagation> propagations, ValueEnqueuedEvent condition) {
        if (!condition.value.getName().equals(Names.condition)) {
            return Collections.emptyList();
        }
        Check.preCondition(condition.value.get() instanceof Boolean);
        return propagations.stream()
                .filter(t -> servesBranchChosen(condition.value.getAs(Boolean.class), t.propagationType))
                .collect(Collectors.toList());
    }

    @Override
    protected void handleAfterlife(AfterlifeEvent k) {
        Check.condition(k instanceof AfterlifeEventCanPropagatePendingValues);
        propagateStashedValues();
    }

    @Override
    protected Optional<AfterlifeEvent> getAfterlife(ValueEnqueuedEvent v) {
        if (Names.condition.equals(v.value.getName())) {
            return Optional.of(new AfterlifeEventCanPropagatePendingValues(this));
        }
        return Optional.empty();
    }

    @Override
    public void processValueDownstream(Value v) {
        Check.preCondition(isDownstream(v.getName()));

        if (Names.condition.equals(v.getName())) {
            Check.preCondition(condition == null);
            Check.invariant(v.get() instanceof Boolean, "condition value must be boolean");
            this.condition = (Boolean) v.get();
        } else {
            if (condition == null) {
                stash.add(v);
            }
            propagateConditionally(v);
        }
    }

    private void propagateConditionally(Value v) {
        getPropagations(v.getName()).stream()
                .filter(p1 -> !isConditioned(p1) || servesBranchChosen(condition, p1.getPropagationType()))
                .forEach(p -> propagate(p, v));
    }

    private void propagateStashedValues() {
        Check.preCondition(condition != null);

        stash.forEach(
                v -> getPropagations(v.getName()).stream()
                        .filter(p1 -> p1.getPropagationType() != PropagationType.COND_CONDITION)
                        .forEach(p -> propagate(p, v))
        );

        stash.clear();
    }

    private boolean isConditioned(ExPropagation p) {
        return p.getPropagationType().in(COND_FALSE_BRANCH, COND_TRUE_BRANCH);
    }

    private boolean servesBranchChosen(boolean condition, PropagationType pType) {
        return (condition && pType == COND_TRUE_BRANCH) || (!condition && pType == COND_FALSE_BRANCH);
    }


}
