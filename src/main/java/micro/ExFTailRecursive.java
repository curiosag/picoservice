package micro;

import micro.event.KarmaEvent;
import micro.event.KarmaEventCanPropagatePendingValues;
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
    protected void handleKarma(KarmaEvent k) {
        Check.invariant(k instanceof KarmaEventCanPropagatePendingValues);
        stash.forEach(this::propagate);
        stash.clear();
        clear(this);
    }

    @Override
    protected Optional<KarmaEvent> getAfterlife(ValueEnqueuedEvent e) {
        if(paramsReceived.size() == template.numParams()) {
            return Optional.of(new KarmaEventCanPropagatePendingValues(this));
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
