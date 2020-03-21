package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import micro.exevent.ExEvent;
import micro.exevent.ValueReceivedEvent;
import micro.exevent.ValueProcessedEvent;

import java.util.*;

public abstract class Ex implements _Ex, KryoSerializable {
    final Env env;
    private long id = -1;
    public F template;
    protected _Ex returnTo;


    private final HashMap<String, List<ExPropagation>> propagationsByParamName = new HashMap<>();

    private final HashSet<String> valuesProcessed = new HashSet<>();
    final Map<String, Value> paramsReceived = new HashMap<>();

    public Ex(Env env) {
        this.env = env;
    }

    public Ex(Env env, F template, _Ex returnTo) {
        this(env);
        this.returnTo = returnTo;
        this.template = template;
        createExPropagations(template);
        env.addX(this);
    }

    @Override
    public _Ex returnTo() {
        return returnTo;
    }

    @Override
    public Address getAddress() {
        return env.getAddress();
    }

    @Override
    public void receive(Value v) {
        raise(new ValueReceivedEvent(this, v));
    }

    public void raise(ExEvent e) {
        env.noteEvent(e);
    }

    public void handle(ExEvent e) {
        if (e instanceof ValueReceivedEvent) {
            ValueReceivedEvent va = (ValueReceivedEvent) e;
            Value v = va.value;

            alterStateFor(va);
            processValueReceived(v);
            return;
        }
        if (e instanceof ValueProcessedEvent) {
            alterStateFor((ValueProcessedEvent) e);
            return;
        }
        Check.fail("unhandled event " + e.toString());
    }

    String getReturnValueName() {
        return template.returnAs;
    }

    private void processValueReceived(Value v) {
        if (!valuesProcessed.contains(v.getName())) {

            switch (v.getName()) {
                case Names.result:
                    returnTo.receive(new Value(getReturnValueName(), v.get(), this));
                    break;

                case Names.exception:
                    returnTo.receive(v.withSender(this));
                    break;

                default:
                    process(v);
            }
            raise(new ValueProcessedEvent(this, v.getName()));
        }
    }

    private void recover(ExEvent e) {
        if (e instanceof ValueReceivedEvent) {
            alterStateFor((ValueReceivedEvent) e);
            return;
        }
        if (e instanceof ValueProcessedEvent) {
            alterStateFor((ValueProcessedEvent) e);
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

    protected abstract void process(Value v);

    private void createExPropagations(F template) {
        template.getTargetFunctionsToPropagations().forEach(
                (targetFunc, templateProps) -> {

                    ExOnDemand to = new ExOnDemand(() -> targetFunc.createExecution(env, this));
                    templateProps.stream()
                            .map(t -> new ExPropagation(t, to))
                            .forEach(p -> propagationsByParamName
                                    .computeIfAbsent(p.getNameReceived(), k -> new ArrayList<>())
                                    .add(p));
                });
    }

    public String getLabel() {
        return template.getLabel();
    }

    protected List<ExPropagation> getPropagations(String paramName) {
        List<ExPropagation> ps = propagationsByParamName.get(paramName);
        return ps != null ? ps : Collections.emptyList();
    }

    protected Value value(String name, Object value) {
        return new Value(name, value, this);
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
