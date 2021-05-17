package micro;

import micro.event.*;
import nano.ingredients.guards.Guards;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public abstract class Ex implements _Ex, Crank {
    protected static final ExEvent none = new ExEvent() {
    };

    protected boolean resultOrExceptionFromPrimitive;

    boolean done = false;
    protected Node node;
    private long id;

    public F template;
    private _Ex returnTo;

    private List<ExPropagation> propagations;
    private final HashSet<String> namesReceived = new HashSet<>();
    final Map<String, Value> paramsReceived = new HashMap<>();

    protected Queue<Value> inBox = new ConcurrentLinkedQueue<>();
    protected Stack<ExEvent> exStack = new Stack<>();
    protected Queue<KarmaEvent> exValueAfterlife = new LinkedList<>();
    protected boolean isRecovery;
    private boolean recovered;

    public Ex(Node node, long id, F template, _Ex returnTo) {
        Guards.notNull(template);
        Guards.notNull(returnTo);

        this.node = node;
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
        return node.getAddress();
    }

    @Override
    public void receive(Value v) {
        if (done) {
            return;
        }
        node.note(new ValueReceivedEvent(this, v));
        inBox.add(v);
    }

    void proceed() {
        if (done) {
            return;
        }

        ExEvent current = exStack.peek();

        if (current instanceof KarmaEvent k) {
            handleKarma(k);
            exStack.pop();
            return;
        }

        if (customEventHandled(current)) {
            exStack.pop();
            return;
        }

        if (current instanceof PropagationTargetExsCreatedEvent) {
            initializePropagationTargets((PropagationTargetExsCreatedEvent) current);
            exStack.pop();
            return;
        }

        if (current instanceof ValueEnqueuedEvent valueEvent) {
            if (triggeredPropagationTargetExsCreatedEvent())
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
            // eventChainedAfterProcessValue mechanism isn't suitable.
            // Its for the case when a single value V processed causes the whole subsequent execution tree
            // to execute, followed by the final enclosing ValueProcessedEvent(V). Such an event log prevents reasonable recovery.
            // (ValueEnqueuedEvent(V) -> all the executions x1...xn until result -> ValueProcessedEvent(V). Imagine shutdown at xi, but
            // on recovery ValueEnqueuedEvent would reinitiate x1...xn in the absence of ValueProcessedEvent. So some mechanism is needed
            // to resolve such scenarios). Its relevant for the cases with potentially stashed pending values,
            // ExFCallByFunctionalValue and ExIf
            getAfterlife(valueEvent).ifPresent(this::extendAfterlife);

            push(new ValueProcessedEvent(this, valueEvent.value));
            return;
        }

        if (current instanceof ValueProcessedEvent) {

            Value value = ((ValueProcessedEvent) current).value;
            clearValue(value);

            if (resultOrException(value) || resultOrExceptionFromPrimitive) {
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

    protected void handleKarma(KarmaEvent k) {
        throw new IllegalStateException("implementation mising?");
    }

    private void extendAfterlife(KarmaEvent karma) {
        node.note(karma);
        exValueAfterlife.add(karma);
    }

    protected boolean customEventHandled(ExEvent current) {
        return false;
    }

    private boolean triggeredPropagationTargetExsCreatedEvent() {
        if (propagations == null) {
            if (template.getTargets().size() > 0) {
                push(new PropagationTargetExsCreatedEvent(this, node.allocatePropagationTargets(this, template.getTargets())));
                return true;
            } else {
                propagations = Collections.emptyList();
            }
        }
        return false;
    }

    private boolean eventChainedBeforeProcessValue(ValueEnqueuedEvent current) {
        ExEvent triggered = getEventTriggeredBeforeCurrent(current);
        if (triggered != none) {
            push(triggered);
            return true;
        }
        return false;
    }

    protected Optional<KarmaEvent> getAfterlife(ValueEnqueuedEvent valueEvent) {
        return Optional.empty();
    }

    private boolean eventChainedAfterProcessValue(ValueEnqueuedEvent current) {
        ExEvent triggered = getEventTriggeredAfterCurrent(current);
        if (triggered != none) {
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

    private Value createResultValue(Value v) {
        return new Value(getNameForReturnValue(), v.get(), this);
    }

    public void recover(ExEvent e) {
        isRecovery = true;
        recovered = true;
        try {
            if (e instanceof KarmaEvent k) {
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
                initializePropagationTargets((PropagationTargetExsCreatedEvent) e);
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
                clearValue(v);
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
            return; // So the caller decides if there's more to do
        }

        if (!exValueAfterlife.isEmpty()) {
            exStack.addAll(exValueAfterlife);
            exValueAfterlife.clear();
            process(exStack);
            return; // So the caller decides if there's more to do
        }

        Value value = inBox.peek();
        Check.invariant(value != null);

        push(new ValueEnqueuedEvent(this, value));
        process(exStack);
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
        node.note(e);
        exStack.push(e);
    }

    private void clearValue(Value e) {
        Check.condition(e.equals(inBox.poll()));
        Check.condition(exStack.pop() instanceof ValueProcessedEvent);
        Check.condition(exStack.pop() instanceof ValueEnqueuedEvent);
    }

    void clear() {
        returnTo = null;
        propagations.clear();
        namesReceived.clear();
        paramsReceived.clear();
        done = true;
    }

    String getNameForReturnValue() {
        return template.returnAs;
    }

    private void initializePropagationTargets(PropagationTargetExsCreatedEvent e) {
        propagations = template.getPropagations().stream()
                .map(t -> new ExPropagation(t, pick(t.target, e.targets)))
                .collect(Collectors.toList());
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

    protected List<ExPropagation> getPropagations(String paramName) {
        return isRecovery ? Collections.emptyList() : propagations.stream()
                .filter(p -> p.getNameReceived().equals(paramName))
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

    protected boolean isLegitDownstreamValue(Value v) {
        return !(Names.result.equals(v.getName()) || Names.exception.equals(v.getName()));
    }

    protected void propagate(Value v) {
        List<ExPropagation> propagations = getPropagations(v.getName());
        propagations.forEach(p ->
                // propagation regardless of recovery or not, since it may have ended up incomplete in an original run
                // A per-propagation state tracking seems too slow and not needed right now but is an option to prevent
                // re-sending on the sender's side
                deliver(new Value(p.getNameToPropagate(), v.get(), this), p.getTo()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s", template.getLabel());
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
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
}
