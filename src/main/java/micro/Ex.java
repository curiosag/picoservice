package micro;

import micro.If.ExIf;
import micro.event.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public abstract class Ex implements _Ex, Crank {
    protected static final ExEvent none = new ExEvent() {
    };

    protected boolean resultOrExceptionFromPrimitive;

    boolean done = false;
    protected Env env;
    private long id;

    public F template;
    private _Ex returnTo;

    protected List<ExPropagation> propagations = new ArrayList<>();
    protected final HashSet<String> namesReceived = new HashSet<>();
    final Map<String, Value> paramsReceived = new HashMap<>();

    protected Queue<Value> inBox = new ConcurrentLinkedQueue<>();
    protected Stack<ExEvent> exStack = new Stack<>();
    protected Queue<AfterlifeEvent> exValueAfterlife = new ArrayDeque<>();
    protected boolean isRecovery;
    protected boolean recovered;

    public Ex(Env env, long id, F template, _Ex returnTo) {
        Guards.notNull(template);
        Guards.notNull(returnTo);

        this.env = env;
        this.id = id;
        this.returnTo = returnTo;
        this.template = template;
    }

    @Override
    public _Ex returnTo() {
        return returnTo;
    }

    @Override
    public _F getTemplate() {
        return template;
    }

    @Override
    public Address getAddress() {
        return env.getAddress();
    }

    @Override
    public void receive(Value v) {
        if (done) {
            return;
        }
        env.note(new ValueReceivedEvent(this, v));
        inBox.add(v);
    }

    /**
     *
     * proceed() is the whole event/state change machinery built around the central element processValue(valueEvent.value);
     * which deals with the value as such
     *
     * */
    void proceed() {
        if (done) {
            return;
        }

        ExEvent current = exStack.peek();

        if (current instanceof AfterlifeEvent k) {
            handleAfterlife(k);
            exStack.pop();
            return;
        }

        if (customEventHandled(current)) {
            exStack.pop();
            return;
        }

        if (current instanceof PropagationTargetExsCreatedEvent) {
            if(recovered && this instanceof ExFTailRecursive && ! propagations.isEmpty())
            {
                propagations.clear();
            }
            addPropagationTargets((PropagationTargetExsCreatedEvent) current);
            exStack.pop();
            return;
        }

        if (current instanceof ValueEnqueuedEvent valueEvent) {
            if (triggerPropagationTargetExsCreatedEvent(valueEvent))
                return;

            if (eventChainedBeforeProcessValue(valueEvent)) {
                return;
            }

            processValue(valueEvent.value);

            // The valueEnqueued/ValueProcessed transactionality is needed in any case, so eventChainedAfterProcessValue
            // must react to ValueEnqeuedEvent rather than ValueProcessedEvent. This implies that processValue must be idempotent
            // at the 2nd call when ValueEnqueuedEvent is processed again after eventChainedAfterProcessValue returned true
            if (eventChainedAfterProcessValue(valueEvent))
                return;

            // afterlife is a recoverable way to trigger events tied to a value V after ValueProcessedEvent(V) when the
            // eventChainedAfterProcessValue mechanism isn't suitable. Its an internal alternative inBox with priority over the actual inBox.
            // Its for the case when a single value V processed causes the whole subsequent execution tree
            // to execute, followed by the final enclosing ValueProcessedEvent(V). Such an event log prevents reasonable recovery.
            // (ValueEnqueuedEvent(V) -> all the executions x1...xn until result -> ValueProcessedEvent(V). Imagine shutdown at xi, but
            // on recovery ValueEnqueuedEvent would reinitiate x1...xn in the absence of ValueProcessedEvent. So some mechanism is needed
            // to resolve such scenarios). Its relevant for the cases with potentially stashed pending values,
            // ExFCallByFunctionalValue and ExIf
            getAfterlife(valueEvent).ifPresent(this::extendAfterlife); //TODO recovery: sort out afterlife without ValueProcessedEvent
                                                                       // TODO there will be the testing issues that a valid afterlive must not be
            // artificially truncated on test

            push(new ValueProcessedEvent(this, valueEvent.value));
            return;
        }

        if (current instanceof ValueProcessedEvent e) {
            Value value = ((ValueProcessedEvent) current).value;
            clearLists(value);
            if (resultOrExceptionFromPrimitive || resultOrException(value) || meansDone(namesReceived)) {
                push(new ExDoneEvent(this));
            }

            return;
        }

        if (current instanceof ExDoneEvent) {
            if (recovered) {
                exValueAfterlife.clear();
            } else {
                Check.postCondition(exValueAfterlife.isEmpty());
            }
            exStack.pop();
            Check.postCondition(exStack.isEmpty());
            done = true;
            return;
        }

        Check.fail("unhandled event " + current.toString());
    }

    private boolean meansDone(HashSet<String> namesReceived) {
        return !template.meansDone.isEmpty() && namesReceived.containsAll(template.meansDone);
    }

    protected void handleAfterlife(AfterlifeEvent k) {
        throw new IllegalStateException("implementation mising?");
    }

    private void extendAfterlife(AfterlifeEvent Afterlife) {
        env.note(Afterlife);
        exValueAfterlife.add(Afterlife);
    }

    protected boolean customEventHandled(ExEvent current) {
        return false;
    }

    protected boolean triggerPropagationTargetExsCreatedEvent(ValueEnqueuedEvent valueEvent) {
        if (needsInitialTargets(valueEvent)) {
            push(new PropagationTargetExsCreatedEvent(this, env.createTargets(this, template.getTargets())));
            return true;
        }
        return false;
    }

    protected boolean needsInitialTargets(ValueEnqueuedEvent e) {
        return template.getTargets().size() > 0 && propagations.size() == 0;
    }

    private boolean eventChainedBeforeProcessValue(ValueEnqueuedEvent current) {
        ExEvent triggered = getEventTriggeredBeforeCurrent(current);
        if (triggered != none) {
            push(triggered);
            return true;
        }
        return false;
    }

    protected Optional<AfterlifeEvent> getAfterlife(ValueEnqueuedEvent valueEvent) {
        return Optional.empty();
    }

    private boolean eventChainedAfterProcessValue(ValueEnqueuedEvent current) {
        ExEvent triggered = getEventTriggeredAfterCurrent(current);
        if(triggered != none) {
            push(triggered);
            return true;
        }
        return false;
    }

    protected ExEvent getEventTriggeredBeforeCurrent(ValueEnqueuedEvent value) {
        return none;
    }

    protected ExEvent getEventTriggeredAfterCurrent(ValueEnqueuedEvent value) {
        return none;
    }

    protected void processValue(Value v) {

        if (namesReceived.contains(v.getName())) {
            // receiving/processValue needs to be idempotent
            //
            // Propagations may re-send values in case of a recovery after an unexpected halt (out of memory, power cut...)
            // where the propagation may even have been completed, but the ValueProcessedEvent not been persisted.
            // Also eventChainedAfterCurrent causes a 2nd call of processValue that should return immediately.
            return;
        }

        namesReceived.add(v.getName());

        if (resultOrException(v)) {
            Value retVal = v.getName().equals(Names.result) ? createResultValue(v) : v.withSender(this);
            deliverResult(retVal);
        } else {
            namesReceived.add(v.getName());
            if (template.formalParameters.contains(v.getName())) {
                paramsReceived.put(v.getName(), v);
            }
            processValueDownstream(v);
        }
    }

    private boolean resultOrException(Value v) {
        return v.getName().equals(Names.result) || v.getName().equals(Names.exception);
    }

    protected void deliverResult(Value v) {
        deliver(v, returnTo);
    }

    protected void deliver(Value v, _Ex target) {
        if (!isRecovery) {
            target.receive(v);
        }
    }

    protected void propagate(ExPropagation p, Value v) {
        if (!isRecovery) {
            p.getTo().receive(Value.of(p.getNameToPropagate(), v.get(), this));
        }
    }

    private Value createResultValue(Value v) {
        return new Value(getNameForReturnValue(), v.get(), this);
    }

    public void recover(ExEvent e) {
        isRecovery = true;
        recovered = true;
        try {
            if (e instanceof AfterlifeEvent k) {
                exValueAfterlife.add(k);
                return;
            }

            if (customEventHandled(e)) {
                return;
            }

            if (e instanceof ValueReceivedEvent) {
                inBox.add(((ValueReceivedEvent) e).value);
                return;
            }

            if (e instanceof PropagationTargetExsCreatedEvent) {
                addPropagationTargets((PropagationTargetExsCreatedEvent) e);
                return;
            }

            if (e instanceof ValueEnqueuedEvent) {
                exStack.push(e);
                return;
            }

            if (e instanceof ValueProcessedEvent) {
                Value v = ((ValueProcessedEvent) e).value;
                processValue(v);
                exStack.push(e);
                clearLists(v);
                return;
            }

            if (e instanceof ExDoneEvent) {
                done = true;
            }
        } finally {
            isRecovery = false;
        }
    }

    protected abstract void processValueDownstream(Value v);

    @Override
    public void crank() {
        Check.invariant(!isRecovery);
        Check.invariant(exStack.isEmpty() || recovered);

        if (recovered && !exStack.isEmpty()) {
            process(exStack);
            return;
        }

        if (!exValueAfterlife.isEmpty()) {
            exStack.addAll(exValueAfterlife);
            exValueAfterlife.clear();
            process(exStack);
            return;
        }

        Value value = inBox.peek();
        Check.invariant(value != null);

        push(new ValueEnqueuedEvent(this, value));
        process(exStack);
        Check.postCondition(exStack.isEmpty());
    }

    private void process(Stack<ExEvent> exStack) {
        while (!exStack.isEmpty()) {
            proceed();
        }
    }

    @Override
    public boolean isMoreToDoRightNow() {
        // inbox might be populated in some re-send recovery cases, where a result
        // already has been produced. Therefore *done* must be considered too
        // afterlife should be processed immediately after valueProcessed, so should be ok with done-logic here too
        return (!(done || (inBox.isEmpty()))) || !exValueAfterlife.isEmpty();
    }

    protected void push(ExEvent e) {
        if (isRecovery) {
            return;
        }
        env.note(e);
        exStack.push(e);
    }

    private void clearLists(Value e) {
        Check.condition(e.equals(inBox.poll()));
        Check.condition(exStack.pop() instanceof ValueProcessedEvent);
        Check.condition(exStack.pop() instanceof ValueEnqueuedEvent);
    }

    String getNameForReturnValue() {
        return template.returnAs;
    }

    protected void addPropagationTargets(PropagationTargetExsCreatedEvent e) {
        Check.invariant(propagations.isEmpty() || this instanceof ExIf);

        propagations.addAll(template.getPropagations().stream()
                .filter(t -> e.targetTemplates.contains(t.target))
                .map(t -> new ExPropagation(t, pick(t.target, e.targets)))
                .collect(Collectors.toList()));
    }

    private _Ex pick(_F targetTemplate, List<_Ex> targets) {
        Optional<_Ex> result = targets.stream()
                .filter(t -> t.getTemplate().equals(targetTemplate))
                .findAny();
        if (result.isEmpty()) {
            throw new IllegalStateException();
        }
        return result.get();
    }

    protected List<ExPropagation> getPropagations(String valueName) {
        return isRecovery ? Collections.emptyList() : propagations.stream()
                .filter(p -> p.getNameReceived().equals(valueName))
                .collect(Collectors.toList());
    }

    @Override
    public String getLabel() {
        return template.getLabel();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long value) {
        throw new IllegalStateException("noooo call this!");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Ex) o).id;
    }

    protected void propagate(Value v) {
        List<ExPropagation> propagations = getPropagations(v.getName());
        propagations.forEach(p ->
                // propagation regardless of recovery or not, since it may have ended up incomplete in an original run
                // A per-propagation state tracking seems too slow and not needed right now but is an option to prevent
                // re-sending on the sender's side
                propagate(p, v));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s", template.getLabel());
    }

    public Env getNode() {
        return env;
    }

    public void setNode(Env env) {
        this.env = env;
    }

    public _Ex getReturnTo() {
        return returnTo;
    }

    public void setReturnTo(_Ex returnTo) {
        this.returnTo = returnTo;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    protected void straightenOutPostRecovery(){};
}
