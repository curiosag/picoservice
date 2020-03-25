package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.event.*;

import java.util.*;

public abstract class Ex implements _Ex, KryoSerializable {
    protected final Node node;
    private long id = -1;
    public F template;
    protected _Ex returnTo;

    private final HashMap<String, List<ExPropagation>> paramNameToPropagations = new HashMap<>();
    private final HashSet<String> valuesProcessed = new HashSet<>();
    final Map<String, Value> paramsReceived = new HashMap<>();

    public Ex(Node node) {
        this.node = node;
    }

    public Ex(Node node, F template, _Ex returnTo) {
        this(node);
        this.returnTo = returnTo;
        this.template = template;
        createExPropagations(template);
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
        raise(new ValueReceivedEvent(node.getNextObjectId(), this, v));
    }

    protected void raise(ExEvent e) {
        e.setId(node.getNextObjectId());
        node.note(e);
    }

    public void handle(ExEvent e) {
        if (e instanceof ValueReceivedEvent) {
            ValueReceivedEvent va = (ValueReceivedEvent) e;
            alterStateFor(va);
            perform(va);
            return;
        }
        if (e instanceof ValueProcessedEvent) {
            alterStateFor((ValueProcessedEvent) e);
            return;
        }
        if (e instanceof PropagateValueEvent) {
            perform((PropagateValueEvent) e);
            return;
        }
        Check.fail("unhandled event " + e.toString());
    }

    public void recover(ExEvent e) {
        if (e instanceof ValueReceivedEvent) {
            alterStateFor((ValueReceivedEvent) e);
            return;
        }
        if (e instanceof ValueProcessedEvent) {
            alterStateFor((ValueProcessedEvent) e);
            return;
        }
        if (e instanceof PropagateValueEvent) {
            return;
        }
        Check.fail("unhandled event " + e.toString());
    }

    private void alterStateFor(ValueReceivedEvent va) {
        Value v = va.value;
        if (!valuesProcessed.contains(v.getName())) {
            if (template.formalParameters.contains(v.getName())) {
                paramsReceived.put(v.getName(), v);
            }
        }
    }

    private void alterStateFor(ValueProcessedEvent v) {
        valuesProcessed.add(v.valueName);
    }

    private void perform(ValueReceivedEvent va) {
        Value v = va.value;
        if (!valuesProcessed.contains(v.getName())) {

            switch (v.getName()) {
                case Names.result:
                    returnTo.receive(new Value(getNameForReturnValue(), v.get(), this));
                    break;

                case Names.exception:
                    returnTo.receive(v.withSender(this));
                    break;

                default:
                    perfromFunctionInputValueReceived(v);
            }
            raise(new ValueProcessedEvent(node.getNextObjectId(), this, v.getName()));
        }
    }

    String getNameForReturnValue() {
        return template.returnAs;
    }

    private void perform(PropagateValueEvent v) {
        v.to.receive(v.value);
    }

    protected abstract void perfromFunctionInputValueReceived(Value v);

    private void createExPropagations(F template) {
        template.getTargetFunctionsToPropagations().forEach(this::createPropagationsForTargetFunc);
    }

    private void createPropagationsForTargetFunc(_F targetFunc, List<FPropagation> templateProps) {
        ExOnDemand to = new ExOnDemand(node, targetFunc, this);
        templateProps.stream()
                .map(t -> new ExPropagation(t, to))
                .forEach(p -> paramNameToPropagations
                        .computeIfAbsent(p.getNameReceived(), k -> new ArrayList<>())
                        .add(p));
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
        id = checkSetIdValue(value);
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

}
