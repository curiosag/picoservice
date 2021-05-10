package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.event.*;
import nano.ingredients.guards.Guards;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Ex implements _Ex, Crank, KryoSerializable {
    boolean done = false;
    protected final Node node;
    private final long id;
    public F template;
    protected _Ex returnTo;

    private boolean idsAllocated;
    Stack<Long> idsToAssign = new Stack<>(); // lowest pops first

    private final HashMap<String, List<ExPropagation>> nameToPropagations = new HashMap<>();
    private final HashMap<_F, _Ex> functionTargetsToFunctionExecutions = new HashMap<>();
    private final HashSet<String> namesReceived = new HashSet<>();
    final Map<String, Value> paramsReceived = new HashMap<>();

    protected Queue<Value> inBox = new ConcurrentLinkedQueue<>();
    protected Stack<ExEvent> exStack = new Stack<>();
    private boolean isRecovery;

    public Ex(Node node, long id) {
        Guards.notNull(node);
        this.node = node;
        this.id = id;
        node.register(this);
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

    protected int getNumberOfIdsNeededForPropagationTargets() {
        return template.getTargetCount();
    }

    protected int getNumberCustomIdsNeeded() {
        return 0;
    }

    void proceed() {
        if (done) {
            return;
        }

        ExEvent current = exStack.peek();
        if (current instanceof IdsAllocatedEvent) {
            initializePropagationTargets((IdsAllocatedEvent) current, template);
            exStack.pop();
            return;
        }

        if (current instanceof ValueEnqueuedEvent) {
            if (!idsAllocated) {
                int idsNeeded = getNumberOfIdsNeededForPropagationTargets() + getNumberCustomIdsNeeded();
                push(new IdsAllocatedEvent(this, node.allocateIds(idsNeeded)));
            } else {
                Value value = ((ValueEnqueuedEvent) current).value;
                processValue(value);
                push(new ValueProcessedEvent(this, value));
            }
            return;
        }

        if (current instanceof ValueProcessedEvent) {
            ValueProcessedEvent c = (ValueProcessedEvent) current;
            clearValue(c);
            return;
        }

        if (current instanceof ExDoneEvent) {
            done = true;
            return;
        }

        Check.fail("unhandled event " + current.toString());
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
            Value retVal = v.getName().equals(Names.result) ?
                    new Value(getNameForReturnValue(), v.get(), this) :
                    v.withSender(this);
            returnTo.receive(retVal);
            push(new ExDoneEvent(this));
        } else {
            namesReceived.add(v.getName());
            if (template.formalParameters.contains(v.getName())) {
                paramsReceived.put(v.getName(), v);
            }
            processValueDownstream(v);
        }
    }

    public void recover(ExEvent e) {
        isRecovery = true;
        if (e instanceof ValueReceivedEvent) {
            inBox.add(((ValueReceivedEvent) e).value);
            return;
        }

        if (e instanceof IdsAllocatedEvent) {
            initializePropagationTargets((IdsAllocatedEvent) e, template);
            return;
        }

        if (e instanceof ValueEnqueuedEvent) {
            exStack.push(e);
            return;
        }

        if (e instanceof ValueProcessedEvent) {
            clearValue((ValueProcessedEvent) e);
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

    private void clearValue(ValueProcessedEvent e) {
        Check.condition(e.value.equals(inBox.poll()));
        exStack.clear();
    }

    void clear() {
        returnTo = null;
        nameToPropagations.clear();
        namesReceived.clear();
        paramsReceived.clear();
        done = true;
    }

    String getNameForReturnValue() {
        return template.returnAs;
    }

    private void initializePropagationTargets(IdsAllocatedEvent e, F template) {
        for (long i = e.rangeTo; i >= e.rangeFrom; i--) {
            idsToAssign.push(i);
        }
        idsAllocated = true;
        template.getTargetFunctionsToPropagations().forEach(this::createPropagationsForTargetFunc);
    }

    private void createPropagationsForTargetFunc(_F targetFunc, List<FPropagation> propagationTemplates) {
        propagationTemplates.stream()
                .map(t -> getOrCreateExPropagation(targetFunc, t))
                .forEach(p -> nameToPropagations
                        .computeIfAbsent(p.getNameReceived(), k -> new ArrayList<>())
                        .add(p));
    }

    private ExPropagation getOrCreateExPropagation(_F targetFunc, FPropagation t) {
        _Ex ex = functionTargetsToFunctionExecutions.computeIfAbsent(targetFunc, i -> i.createExecution(getNextExId(), this));
        return new ExPropagation(t, ex);
    }

    protected Long getNextExId() {
        Check.preCondition(!idsToAssign.isEmpty());
        return idsToAssign.pop();
    }

    protected List<ExPropagation> getPropagations(String paramName) {
        List<ExPropagation> ps = nameToPropagations.get(paramName);

        return ps != null ? ps : Collections.emptyList();
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
                p.getTo().receive(new Value(p.getNameToPropagate(), v.get(), this)));
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
