package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Ex implements _Ex, KryoSerializable {
    final Env env;
    private long id = -1;
    public F template;
    protected _Ex returnTo;

    private final HashMap<String, List<ExPropagation>> propagationsByParamName = new HashMap<>();
    private final List<ExPropagation> propagations = new ArrayList<>();

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
    public void accept(Value v) {
        env.enq(v, this);
    }

    @Override
    public Address getAddress() {
        return env.getAddress();
    }

    public abstract void process(Value v);

    protected abstract void propagate(Value v);

    protected void registerReceived(Value v) {
        env.debug(v.getSender() + " -> " + this + " " + v.toString());
        if (template.formalParameters.contains(v.getName())) {
            paramsReceived.put(v.getName(), v);
        }
    }

    private void createExPropagations(F template) {
        template.getTargetFunctionsToPropagations().forEach(
                (targetFunc, templateProps) -> {

                    List<ExPropagation> exProps = templateProps.stream()
                            .map(this::createPropagation)
                            .collect(Collectors.toList());

                    exProps.forEach(p -> {
                        p.setHavingSameTarget(exProps);
                        propagationsByParamName.computeIfAbsent(p.template.nameReceived, k -> new ArrayList<>()).add(p);
                        propagations.add(p);
                    });

                });
    }

    public String getLabel() {
        return template.getLabel();
    }

    private ExPropagation createPropagation(FPropagation t) {
        return new ExPropagation(env, this, t);
    }

    protected List<ExPropagation> getPropagations(String paramName) {
        List<ExPropagation> ps = propagationsByParamName.get(paramName);
        return ps == null ? Collections.emptyList() : ps.stream().filter(p -> !p.isDone()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("%s", template.getLabel());
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
        id = checkSetValue(value);
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
    public void write(Kryo kryo, Output output) {
        output.writeVarLong(template.getId(), true);
        output.writeVarLong(id, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {

    }
}
