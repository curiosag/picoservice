package micro;

import micro.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ExFCallByFunctionalValue extends Ex {

    private enum RecoveryState {
        COLLECTING_PARAMS, LAST_VAL_ENQ
    }

    private record RecoveryTransition(RecoveryState from,
                                      RecoveryState to,
                                      BiFunction<ExEvent, ExFTailRecursive, Boolean> condition,
                                      BiConsumer<ExEvent, ExFTailRecursive> action) {
    }

    BiConsumer<ExEvent, ExFTailRecursive> nop = (a, b) -> {
    };

    private List<RecoveryTransition> recoveryTransitions = List.of(
            new RecoveryTransition(RecoveryState.COLLECTING_PARAMS, RecoveryState.LAST_VAL_ENQ,
                    (e, x) -> e instanceof ValueEnqueuedEvent && paramsReceived.size() == template.numParams() - 1, nop),
            new RecoveryTransition(RecoveryState.LAST_VAL_ENQ, RecoveryState.LAST_VAL_ENQ,
                    (e, x) -> e instanceof PropagationTargetExsCreatedEvent , nop),
            new RecoveryTransition(RecoveryState.LAST_VAL_ENQ, RecoveryState.COLLECTING_PARAMS,
                    (e, x) -> e instanceof ValueProcessedEvent, (e, x) -> x.reset())
    );


    FunctionalValueDefinition fDef;

    _F fValF;
    _Ex fValEx;

    private final List<Value> pendingValues = new ArrayList<>();
    private boolean dependentExCreated;

    ExFCallByFunctionalValue(Env env, long id, FunctionalValueDefinition fDef, _Ex returnTo) {
        super(env, id, fDef, returnTo);
        this.fDef = fDef;
    }

    @Override
    protected void handleAfterlife(AfterlifeEvent k) {
        Check.condition(k instanceof AfterlifeEventCanPropagatePendingValues);
        Check.preCondition(dependentExCreated);

        pendingValues.forEach(pv -> deliver(pv.withSender(this), fValEx));
        pendingValues.clear();
    }

    @Override
    public boolean customEventHandled(ExEvent e) {
        if (e instanceof DependendExCreatedEvent) {
            dependentExCreated = true; // needs to be set in case of recovery
            fValEx = e.getEx();
            Check.postCondition((isRecovery && fValF == null) || fValEx.getTemplate().equals(fValF));
            return true;
        }

        return false;
    }

    @Override
    protected Optional<AfterlifeEvent> getAfterlife(ValueEnqueuedEvent e) {
        if (isFunctionalValueParam(e)) {
            return Optional.of(new AfterlifeEventCanPropagatePendingValues(this));
        }
        return Optional.empty();
    }

    private boolean isFunctionalValueParam(ValueEvent e) {
        return e.value.getName().equals(fDef.getFunctionalValueParam());
    }

    @Override
    protected ExEvent getEventTriggeredAfterCurrent(ValueEnqueuedEvent e) {
        if (!dependentExCreated && isFunctionalValueParam(e)) {
            dependentExCreated = true;
            return env.createDependentExecutionEvent(fValF, this, this);
        }
        return none;
    }

    private void acceptFunctionalValueTemplate(Object value) {
        Check.invariant(value instanceof PartiallyAppliedFunction, "that wasn't expected: " + value);
        @SuppressWarnings("ConstantConditions") PartiallyAppliedFunction f = ((PartiallyAppliedFunction) value);
        fValF = f.baseFunction;
        pendingValues.addAll(f.partialValues);
    }

    @Override
    protected void processValueDownstream(Value v) {
        Check.preCondition(isDownstream(v.getName()));

        if (v.getName().equals(fDef.getFunctionalValueParam())) {
            acceptFunctionalValueTemplate(v.get());
        } else if (fValEx == null) {
            pendingValues.add(v);
        } else {
            deliver(v.withSender(this), getFunctionBeingCalled());
        }
    }

    @Override
    public String getLabel() {
        return fDef.getLabel();
    }

    String getNameForReturnValue() {
        return fDef.returnAs;
    }

    private _Ex getFunctionBeingCalled() {
        Check.invariant(fValEx != null);
        return fValEx;
    }

}
