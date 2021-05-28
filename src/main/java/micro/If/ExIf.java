package micro.If;

import micro.*;
import micro.event.KarmaEvent;
import micro.event.KarmaEventCanPropagatePendingValues;
import micro.event.PropagationTargetExsCreatedEvent;
import micro.event.ValueEnqueuedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static micro.PropagationType.FALSE_BRANCH;
import static micro.PropagationType.TRUE_BRANCH;

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
                            .filter(t -> t.propagationType == PropagationType.CONDITION || t.propagationType == PropagationType.INDISCRIMINATE)
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
            push(new PropagationTargetExsCreatedEvent(this, env.allocatePropagationTargets(this, targets)));
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
        Check.preCondition(isDownstream(v.getName()));

        if (Names.condition.equals(v.getName())) {
            Check.preCondition(condition == null);
            Check.invariant(v.get() instanceof Boolean, "condition value must be boolean");
            this.condition = (Boolean) v.get();
            echoIfCount();
        } else {
            if (condition == null) {
                stash.add(v);
            }
            propagateConditionally(v);
        }
    }

    private void echoIfCount() {
        if (debugIfCount) {
            ((F) getTemplate()).icnt++;
            System.out.print(".");
            if (((F) getTemplate()).icnt % 50 == 0) {
                System.out.print("\n" + ((F) getTemplate()).icnt);
            }
        }
    }

    private void propagateConditionally(Value v) {
        getPropagations(v.getName()).stream()
                .filter(p1 -> !isConditioned(p1) || servesBranchChosen(condition, p1.getPropagationType()))
                .forEach(p -> deliver(new Value(p.getNameToPropagate(), v.get(), this), p.getTo()));
    }

    private void propagateStashedValues() {
        Check.preCondition(condition != null);

        stash.forEach(
                v -> getPropagations(v.getName()).stream()
                        .filter(p1 -> p1.getPropagationType() != PropagationType.CONDITION)
                        .forEach(p -> deliver(Value.of(p.getNameToPropagate(), v.get(), v.getSender()), p.getTo()))
        );

        stash.clear();
    }

    private boolean isConditioned(ExPropagation p) {
        return p.getPropagationType().in(FALSE_BRANCH, TRUE_BRANCH);
    }

    private boolean servesBranchChosen(boolean condition, PropagationType pType) {
        return (condition && pType == TRUE_BRANCH) || (!condition && pType == FALSE_BRANCH);
    }


}
