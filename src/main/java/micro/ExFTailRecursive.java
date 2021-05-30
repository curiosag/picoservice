package micro;

import micro.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExFTailRecursive extends Ex {

    List<Value> stash = new ArrayList<>();

    public ExFTailRecursive(Env env, long id, F template, _Ex returnTo) {
        super(env, id, template, returnTo);
        Check.invariant(template.formalParameters.size() > 0); // that would be a special case with ping and stuff
    }

    private ValueEnqueuedEvent recent;
    @Override
    protected boolean triggerPropagationTargetExsCreatedEvent(ValueEnqueuedEvent valueEvent) {
        // here we need propagations only after all parameters arrived
        if (valueEvent != recent &&!paramsReceived.containsKey(valueEvent.value.getName()) && paramsReceived.size() + 1 == template.numParams()) {
            recent = valueEvent;
            // terminate elements from the recursion who can't terminate the usual way by receiving a result
            stash.stream()
                    .map(Value::getSender)
                    .distinct()
                    .filter(i -> !i.equals(this.returnTo())) // we mustn't terminate the initial non-recursive call
                    .forEach(i -> sendCommand(i, Command.terminateTailRecursionElements));

            // clear out last recursion
            propagations.clear();
            // and get the elements for the new one
            push(new PropagationTargetExsCreatedEvent(this, env.createTargets(this, template.getTargets())));
            return true;
        }
        return false;
    }

    @Override
    protected void handleAfterlife(AfterlifeEvent k) {
        Check.invariant(k instanceof AfterlifeEventCanPropagatePendingValues);
        stash.forEach(this::propagate);
        reset();
    }

    @Override
    protected Optional<AfterlifeEvent> getAfterlife(ValueEnqueuedEvent e) {
        if (paramsReceived.size() == template.numParams()) {
            return Optional.of(new AfterlifeEventCanPropagatePendingValues(this));
        }
        return Optional.empty();
    }

    @Override
    public void processValueDownstream(Value v) {
        if (!template.isParam(v.getName())) {
            throw new IllegalStateException("ExFTailRecursive can only handle values that are parameters");
        }
        stash.add(v);
    }


    protected void reset() {
        Check.preCondition(exStack.stream()
                .map(i -> i instanceof AfterlifeEvent)
                .reduce(true, (a, b) -> a && b));

        Check.preCondition(exValueAfterlife.isEmpty());

        stash.clear();
        namesReceived.clear();
        paramsReceived.clear();
    }


    @Override
    public void recover(ExEvent e) {
        if(e instanceof AfterlifeEventCanPropagatePendingValues)
        {
            propagations.clear();
        }
        super.recover(e);
    }
}
