package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.event.*;
import nano.ingredients.guards.Guards;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class Ex implements _Ex, EventDriven, KryoSerializable {
    boolean done = false;
    protected final Node node;
    private final long id;
    public F template;
    protected _Ex returnTo;

    private boolean idsReserved;
    Stack<Long> idsReservedToAssign = new Stack<>(); // lowest pops first

    private final HashMap<String, List<ExPropagation>> paramNameToPropagations = new HashMap<>();
    private final HashMap<_F, _Ex> propagationTargetsCreated = new HashMap<>();
    private final HashSet<String> valuesReceived = new HashSet<>();
    final Map<String, Value> paramsReceived = new HashMap<>();

    protected ConcurrentLinkedDeque<ExEvent> eventsPendingProcessing = new ConcurrentLinkedDeque<>();

    public Ex(Node node, long id) {
        Guards.notNull(node);
        this.node = node;
        this.id = id;
        node.registerEx(this);
        node.processEvents(this);
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
        if (done || node.isRecovery()) {
            return;
        }

        if (!idsReserved) {
            announce(new IdsReservedEvent(this, node.reserveIds(getDefaultNumberIdsNeeded() + getNumberCustomIdsNeeded())));
            idsReserved = true;
        }
        ValueReceivedEvent event = new ValueReceivedEvent(this, v);
        announce(event);
        //node.debugValueReceived(event);
    }

    protected int getDefaultNumberIdsNeeded() {
        return template.getTargetCount();
    }

    protected int getNumberCustomIdsNeeded() {
        return 0;
    }

    public void recover(ExEvent e) {
        handle(e);
    }

    protected void announce(ExEvent e) {
        node.note(e);
        eventsPendingProcessing.add(e);
    }

    void handle(ExEvent e) {
        if (done) {
            return;
        }
        if (e instanceof ValueReceivedEvent) {
            processValue(((ValueReceivedEvent) e).value);
            return;
        }
        if (e instanceof IdsReservedEvent) {
            IdsReservedEvent ide = (IdsReservedEvent) e;
            for (long i = ide.rangeTo; i >= ide.rangeFrom; i--) {
                idsReservedToAssign.push(i);
            }
            return;
        }
        if(e instanceof ValueProcessedEvent)
        {
            Value value = ((ValueProcessedEvent) e).value;
            //TODO: still need to log processed values
            return;
        }

        Check.fail("unhandled event " + e.toString());
    }

    protected void processValue(Value v) {
        if (valuesReceived.size() == 0) {
            createPropagationTargets(template);
        }

        if (!valuesReceived.contains(v.getName())) {
            valuesReceived.add(v.getName());
            switch (v.getName()) {
                case Names.result:
                    returnTo.receive(new Value(getNameForReturnValue(), v.get(), this));
                    node.stopProcessingEvents(this);
                    break;

                case Names.exception:
                    returnTo.receive(v.withSender(this));
                    node.stopProcessingEvents(this);
                    break;

                default: {
                    if (template.formalParameters.contains(v.getName())) {
                        paramsReceived.put(v.getName(), v);
                    }
                    processDownstreamValue(v);
                }

            }
        }
        announce(new ValueProcessedEvent(this, v));
    }

    protected abstract void processDownstreamValue(Value v);

    public boolean hasNextEvent(){
        return eventsPendingProcessing.size() > 0;
    }

    public void processNextEvent(){
        handle(eventsPendingProcessing.pop());
    }

    void clear() {
        returnTo = null;
        paramNameToPropagations.clear();
        valuesReceived.clear();
        paramsReceived.clear();
        done = true;
    }

    String getNameForReturnValue() {
        return template.returnAs;
    }

    private void createPropagationTargets(F template) {
        template.getTargetFunctionsToPropagations().forEach(this::createPropagationsForTargetFunc);
    }

    private void createPropagationsForTargetFunc(_F targetFunc, List<FPropagation> templateProps) {
        templateProps.stream()
                .map(t -> getOrCreateExPropagation(targetFunc, t))
                .forEach(p -> paramNameToPropagations
                        .computeIfAbsent(p.getNameReceived(), k -> new ArrayList<>())
                        .add(p));
    }

    private ExPropagation getOrCreateExPropagation(_F targetFunc, FPropagation t) {
        _Ex ex = propagationTargetsCreated.computeIfAbsent(targetFunc, i-> i.createExecution(getNextExId(), this));
        return new ExPropagation(t, ex);
    }

    protected Long getNextExId() {
        Check.preCondition(!idsReservedToAssign.isEmpty());
        return idsReservedToAssign.pop();
    }

    protected List<ExPropagation> getPropagations(String paramName) {
        List<ExPropagation> ps = paramNameToPropagations.get(paramName);

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
        throw new IllegalStateException("noooo call!");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Ex) o).id;
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

    protected boolean isLegitDownstreamValue(Value v) {
        return !(Names.result.equals(v.getName()) || Names.exception.equals(v.getName()));
    }

    protected void propagate(Value v) {
        getPropagations(v.getName()).forEach(p ->
                p.getTo().receive(new Value(p.getNameToPropagate(), v.get(), this)));
    }
}
