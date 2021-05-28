package micro;

import micro.event.AfterlifeEvent;
import micro.event.AfterlifeEventCanPropagatePendingValues;
import micro.event.ValueEnqueuedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExFTailRecursive extends Ex {

    List<Value> stash = new ArrayList<>();

    public ExFTailRecursive(Env env, long id, F template, _Ex returnTo) {
        super(env, id, template, returnTo);
        Check.invariant(template.formalParameters.size() > 0); // that would be a special case with ping and stuff
    }

    @Override
    protected void handleAfterlife(AfterlifeEvent k) {
        Check.invariant(k instanceof AfterlifeEventCanPropagatePendingValues);
        stash.forEach(this::propagate);
        stash.clear();
    }

    @Override
    protected Optional<AfterlifeEvent> getAfterlife(ValueEnqueuedEvent e) {
        if(paramsReceived.size() == template.numParams()) {
            return Optional.of(new AfterlifeEventCanPropagatePendingValues(this));
        }
        return Optional.empty();
    }

    @Override
    public void processValueDownstream(Value v) {
        if(!template.isParam(v.getName()))
        {
            throw new IllegalStateException("ExFTailRecursive can only handle values that are parameters");
        }
        stash.add(v);
    }

}
