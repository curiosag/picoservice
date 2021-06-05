package micro;

import micro.event.ExEvent;
import micro.event.PropagationTargetExsCreatedEvent;
import micro.event.ValueEnqueuedEvent;

import java.util.ArrayList;
import java.util.List;

public class ExFTailRecursive extends Ex {

    List<Value> stash = new ArrayList<>();

    public ExFTailRecursive(Env env, long id, F template, _Ex returnTo) {
        super(env, id, template, returnTo);
        Check.invariant(template.formalParameters.size() > 0); // that would be a special case with ping and stuff
    }

    @Override
    protected boolean needsInitialTargets() {
        return false;
    }

    @Override
    protected ExEvent getEventTriggeredAfterCurrent(ValueEnqueuedEvent value) {
        if (paramsReceived.size() == template.numParams()) {
            addPropagationTargets(new PropagationTargetExsCreatedEvent(this, env.createTargets(this, template.getTargets())));
            stash.forEach(this::propagate);
            reset();
        }
        return none;
    }

    @Override
    public void processValueDownstream(Value v) {
        if (!template.isParam(v.getName())) {
            throw new IllegalStateException("ExFTailRecursive can only handle values that are parameters");
        }
        stash.add(v);
    }

    protected void reset() {
        Check.preCondition(exValueAfterlife.isEmpty());

        stash.clear();
        propagations.clear();
        namesReceived.clear();
        paramsReceived.clear();
    }

    @Override
    public void recover(ExEvent e) {
        super.recover(e);

        if (paramsReceived.size() == template.numParams()) {
            reset();
        }
    }

}
