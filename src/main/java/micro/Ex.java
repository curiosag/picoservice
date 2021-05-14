package micro;

import micro.event.*;
import nano.ingredients.guards.Guards;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public abstract class Ex implements _Ex, Crank {
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
    private boolean isRecovery;
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

        switch (customEventHandling(current)) {
            case nonconsuming -> {
                return;
            }
            case consuming -> {
                exStack.pop();
                return;
            }
            case none -> {
            }
        }

        if (current instanceof PropagationTargetExsCreatedEvent) {
            initializePropagationTargets((PropagationTargetExsCreatedEvent) current);
            exStack.pop();
            return;
        }

        if (current instanceof ValueEnqueuedEvent) {
            if (propagations == null) {
                if (template.getTargets().size() > 0) {
                    push(new PropagationTargetExsCreatedEvent(this, node.allocatePropagationTargets(this, template.getTargets())));
                    return;
                } else {
                    propagations = Collections.emptyList();
                }
            }

            Optional<ExEvent> customValueEvent = raiseCustomEvent(((ValueEnqueuedEvent) current).value);
            if (customValueEvent.isPresent()) {
                push(customValueEvent.get());
                return;
            }

            Value value = ((ValueEnqueuedEvent) current).value;
            processValue(value);
            if (resultOrException(value) || resultOrExceptionFromPrimitive) {
                push(new ExDoneEvent(this), new ValueProcessedEvent(this, value));
            } else {
                push(new ValueProcessedEvent(this, value));
            }
            return;
        }

        if (current instanceof ValueProcessedEvent) {
            clearValue(((ValueProcessedEvent) current).value);
            return;
        }

        if (current instanceof ExDoneEvent) {
            done = true;
            return;
        }

        Check.fail("unhandled event " + current.toString());
    }

    protected CustomEventHandling customEventHandling(ExEvent current) {
        return CustomEventHandling.none;
    }

    protected Optional<ExEvent> raiseCustomEvent(Value value) {
        return Optional.empty();
    }

    protected void processValue(Value v) {
        if (namesReceived.contains(v.getName())) {
            // propagations may re-send values in case of a recovery after an unexpected halt (out of memory, power cut...)
            // where the propagation may even have been complete, but the ValueProcessedEvent not been persisted.
            // so receiving/processValue needs to be idempotent
            node.log(getLabel() + " skipping already received value " + v);
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
            switch (customEventHandling(e)) {
                case nonconsuming, consuming -> {
                    return;
                }
                case none -> {
                }
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

        Value value = inBox.peek();
        Check.invariant(value != null);

        push(new ValueEnqueuedEvent(this, value));
        while (!exStack.isEmpty()) {
            proceed();
        }
    }

    @Override
    public boolean isMoreToDo() {
        return !inBox.isEmpty();
    }

    protected void push(ExEvent... e) {
        if (isRecovery) {
            return;
        }
        for (int i = e.length - 1; i >= 0; i--) {
            node.note(e[i]);
        }
        Arrays.stream(e).forEach(exStack::push);
    }

    private void clearValue(Value e) {
        Check.condition(e.equals(inBox.poll()));
        exStack.clear();
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
        getPropagations(v.getName()).forEach(p ->
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

    public void setTemplate(F template) {
        this.template = template;
    }

    public _Ex getReturnTo() {
        return returnTo;
    }

    public void setReturnTo(_Ex returnTo) {
        this.returnTo = returnTo;
    }
}
