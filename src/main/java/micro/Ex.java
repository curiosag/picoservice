package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Ex implements _Ex, KryoSerializable {
    private boolean done = false;
    protected final Node node;
    private long id = -1;
    public F template;
    protected _Ex returnTo;

    private final List<_Ex> targets = new ArrayList<>();
    private final List<String> valuesReceived = new ArrayList<>();
    final List<Value> paramsReceived = new ArrayList<>();

    public Ex(Node node) {
        this.node = node;
    }

    public Ex(Node node, F template, _Ex returnTo) {
        this(node);
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

    protected void enQ(ExEvent e) {
        node.enQ(e);
    }

    @Override
    public void receive(Value v) {
        if (done) {
            return;
        }
        enQ(new ValueReceivedEvent(this, v));
    }

    void handle(ExEvent e) {
        if (done) {
            return;
        }
        if (e instanceof ValueProcessedEvent) {
            return;
        }
        if (e instanceof ProcessValueEvent) {
            ProcessValueEvent va = (ProcessValueEvent) e;
            if (!valuesReceived.contains(va.value.getName())) {
                alterStateFor(va.value);
                perform(va);
            }
            return;
        }
        if (e instanceof PropagateValueEvent) {
            perform((PropagateValueEvent) e);

            return;
        }
        if (e instanceof ExecutionProvidedEvent) {
            alterStateFor((ExecutionProvidedEvent) e);
            return;
        }

        Check.fail("unhandled event " + e.toString());
    }

    void recover(Value v) {
        alterStateFor(v);
    }

    void alterStateFor(ExecutionProvidedEvent e) {
        Check.invariant(e.ex.equals(this), "..?");
        Check.invariant(!targets.contains(e.provided), "..?");

        targets.add(e.provided);
    }

    protected void alterStateFor(Value v) {
        if (template.formalParameters.contains(v.getName())) {
            paramsReceived.add(v);
        }
    }

    private void perform(ProcessValueEvent va) {
        Value v = va.value;
        switch (v.getName()) {
            case Names.result:
                returnTo.receive(new Value(getNameForReturnValue(), v.get(), this));
                //clear();
                break;

            case Names.exception:
                returnTo.receive(v.withSender(this));
                break;

            default:
                processInputValue(v);
        }

        enQ(new ValueProcessedEvent(this, v));
    }

    private void perform(PropagateValueEvent e) {
        _Ex target = targets.stream()
                .filter(t -> t.getTemplate().equals(e.toF))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Target not assigned for template " + e.toF.getId()));
        target.receive(e.value.withSender(this));
    }

    boolean isFunctionInputValue(Value v) {
        return !(Names.result.equals(v.getName()) || Names.exception.equals(v.getName()));
    }

    protected PropagateValueEvent newPropagateValueEvent(Value v, FPropagation p) {
        return new PropagateValueEvent(this, p.target, new Value(p.nameToPropagate, v.get(), this));
    }

    void initiatePropagations(Value v, List<FPropagation> propagations) {
        propagations.stream()
                .map(p -> p.target)
                .distinct()
                .forEach(tt -> {
                    if (targets.stream().noneMatch(t -> t.getTemplate().equals(tt))) {
                        enQ(new ProvideExecutionEvent(this, template));
                    }
                });

        propagations.forEach(p -> enQ(newPropagateValueEvent(v, p)));
    }

    void clear() {
        returnTo = null;
        targets.clear();
        valuesReceived.clear();
        paramsReceived.clear();
        done = true;
    }

    String getNameForReturnValue() {
        return template.returnAs;
    }

    protected abstract void processInputValue(Value v);

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
        Check.invariant(id >= 0, "nö!");
        return Objects.hash(id);
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
