package micro;

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
    public void receive(Value v) {
        // there are tighter restrictions on the content of the inbox in order to properly manage successive calls,
        // so skipping of duplicates has to happen here already
        if (!inBox.contains(v))
            super.receive(v);
    }

    @Override
    public void processValueDownstream(Value v) {
        if (v.get() instanceof Value.Signal signal) {
            Check.preCondition(signal == Value.Signal.PARAMS_COMPLETE);
            Check.preCondition((paramsReceived.size() == template.numParams()));
            stash.forEach(this::propagate);
            // ok to reset after propagation. triggered executions may cause new values in the inbox already,
            // but they only get processed after reset
            reset();
        } else {
            if (!(template.isParam(v.getName()))) {
                throw new IllegalStateException("ExFTailRecursive can only handle values that are parameters");
            }
            stash.add(v);
            if (paramsReceived.size() == template.numParams()) {
                deliver(Value.of(Names.signal, Value.Signal.PARAMS_COMPLETE, this), this);
            }
        }
    }

    protected void reset() {
        stash.clear();
        propagations.clear();
        namesReceived.clear();
        paramsReceived.clear();
    }

    @Override
    protected void straightenOutPostRecovery() {
        Check.preCondition(recovered);
        Check.preCondition(inBox.size() <= 2);
        Optional<Value> sig = inBox.stream().filter(i -> i.get().equals(Value.Signal.PARAMS_COMPLETE)).findAny();
        if (sig.isPresent() && inBox.size() == 2) {
            inBox.remove(sig.get());
        }
    }

}
