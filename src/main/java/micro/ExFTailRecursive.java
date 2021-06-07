package micro;

import micro.event.AfterlifeEvent;
import micro.event.AfterlifeEventCanPropagatePendingValues;
import micro.event.ExEvent;
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
        Check.preCondition(k instanceof AfterlifeEventCanPropagatePendingValues && k.ex.equals(this));

        Check.preCondition((paramsReceived.size() == template.numParams()));
        Check.preCondition(inBox.isEmpty() || considerRecovery());

        stash.forEach(this::propagate);
        // ok to reset after propagation. propagation may put new values in the inbox already, but they only get
        // processed after reset
        reset();
    }

    private boolean considerRecovery() {
        if(recovered){
           if(inBox.size() > 0 && inBox.peek().getName().equals(Names.result)) {
               stash.clear();
           } else if(paramsReceived.size() == template.numParams() && ! inBox.isEmpty())
           {
               inBox.clear();
           }

        }
        return recovered;
    }

    private boolean paramsComplete(String paramName) {
        return paramsReceived.size() == template.numParams() - 1
                && template.isParam(paramName)
                && !paramsReceived.containsKey(paramName);
    }

    protected Optional<AfterlifeEvent> getAfterlife(ValueEnqueuedEvent valueEvent) {
        return paramsReceived.size() == template.numParams() ?
                Optional.of(new AfterlifeEventCanPropagatePendingValues(this)):
                Optional.empty();
    }

    @Override
    public void processValueDownstream(Value v) {
        if (!template.isParam(v.getName())) {
            throw new IllegalStateException("ExFTailRecursive can only handle values that are parameters");
        }
        stash.add(v);
    }

    protected void reset() {
        stash.clear();
        propagations.clear();
        namesReceived.clear();
        paramsReceived.clear();
    }

    @Override
    public void recover(ExEvent e) {
        if (e instanceof ValueEnqueuedEvent && ! exValueAfterlife.isEmpty()) {
            reset();
            exValueAfterlife.clear();

        }
        super.recover(e);
    }

    @Override
    protected void straightenOutPostRecovery() {
        Check.preCondition(recovered);
        // AfterliveEvent written, but ValueProcessedEvent missing
        if(exValueAfterlife.size() == 1 && exStack.size() == 1) {
            Check.preCondition(exValueAfterlife.peek() instanceof AfterlifeEventCanPropagatePendingValues);
            Check.preCondition(exStack.peek() instanceof ValueEnqueuedEvent);
            exValueAfterlife.clear();
        }

    }

}
