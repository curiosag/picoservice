package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.event.*;
import nano.ingredients.guards.Guards;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public abstract class Ex implements _Ex, Crank, KryoSerializable {
    boolean done = false;
    protected final Node node;
    private final long id;
    public F template;
    private _Ex returnTo;

    private List<ExPropagation> propagations;
    private final HashSet<String> namesReceived = new HashSet<>();
    final Map<String, Value> paramsReceived = new HashMap<>();

    protected Queue<Value> inBox = new ConcurrentLinkedQueue<>();
    protected Stack<ExEvent> exStack = new Stack<>();
    private boolean isRecovery;

    public Ex(Node node, long id) {
        Guards.notNull(node);
        this.node = node;
        this.id = id;
    }

    public Ex(Node node, long id, F template, _Ex returnTo) {
        this(node, id);
        Guards.notNull(template);
        Guards.notNull(returnTo);

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

        if (current instanceof PropagationTargetsAllocatedEvent) {
            initializePropagationTargets((PropagationTargetsAllocatedEvent) current);
            exStack.pop();
            return;
        }

        if (current instanceof ValueEnqueuedEvent) {
            if (propagations == null) {
                push(new PropagationTargetsAllocatedEvent(this, node.allocatePropagationTargets(this, template.getTargets())));
                return;
            }

            Optional<ExEvent> customValueEvent = raiseCustomEvent(((ValueEnqueuedEvent) current).value);
            if (customValueEvent.isPresent()) {
                push(customValueEvent.get());
                return;
            }

            Value value = ((ValueEnqueuedEvent) current).value;
            processValue(value);
            push(new ValueProcessedEvent(this, value));

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
            node.log("skipping already received value " + v);
            return;
        }

        namesReceived.add(v.getName());

        if (v.getName().equals(Names.result) || v.getName().equals(Names.exception)) {
            Value retVal = v.getName().equals(Names.result) ? createResultValue(v) : v.withSender(this);
            deliverResult(retVal);
            push(new ExDoneEvent(this));
        } else {
            namesReceived.add(v.getName());
            if (template.formalParameters.contains(v.getName())) {
                paramsReceived.put(v.getName(), v);
            }
            processValueDownstream(v);
        }
    }

    protected void deliverResult(Value v){
       deliver(v, returnTo);
    }

    protected void deliver(Value v, _Ex target){
        if(! isRecovery) {
            target.receive(v);
        }
    }

    private Value createResultValue(Value v) {
        return new Value(getNameForReturnValue(), v.get(), this);
    }

    public void recover(ExEvent e) {
        isRecovery = true;

        switch (customEventHandling(e)) {
            case nonconsuming, none -> {
                return;
            }
            case consuming -> {
                return;
            }
        }

        if (e instanceof ValueReceivedEvent) {
            inBox.add(((ValueReceivedEvent) e).value);
            return;
        }

        if (e instanceof PropagationTargetsAllocatedEvent) {
            initializePropagationTargets((PropagationTargetsAllocatedEvent) e);
            return;
        }

        if (e instanceof ValueEnqueuedEvent) {
            exStack.push(e);
            return;
        }

        if (e instanceof ValueProcessedEvent) {
            Value v = ((ValueProcessedEvent)e).value;
            processValue(v);
            clearValue(v);
            return;
        }

        if (e instanceof ExDoneEvent) {
            done = true;
            return;
        }

        isRecovery = false;
    }

    protected abstract void processValueDownstream(Value v);

    @Override
    public void crank() {
        Check.invariant(!isRecovery);
        Check.invariant(exStack.isEmpty());

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

    protected void push(ExEvent e) {
        if (isRecovery) {
            return;
        }
        node.note(e);
        exStack.push(e);
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

    private void initializePropagationTargets(PropagationTargetsAllocatedEvent e) {
        propagations = template.getPropagations().stream()
                .map(t -> new ExPropagation(t, pick(t.target, e.targets)))
                .collect(Collectors.toList());
    }

    private _Ex pick(_F targetTemplate, List<_Ex> targets) {
        return targets.stream()
                .filter(t -> t.getTemplate().equals(targetTemplate))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }

    protected List<ExPropagation> getPropagations(String paramName) {
        return propagations.stream()
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

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeVarLong(template.getId(), true);
        output.writeVarLong(id, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {

    }

}
