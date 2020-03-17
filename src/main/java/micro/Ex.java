package micro;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Ex implements _Ex {
    final Env env;

    private long id = -1;
    public final F template;
    protected final _Ex returnTo;

    private final HashMap<String, List<ExPropagation>> propagationsByParamName = new HashMap<>();
    private final List<ExPropagation> propagations = new ArrayList<>();

    final Map<String, Value> paramsReceived = new HashMap<>();

    public Ex(Env env, F template, _Ex returnTo) {
        this.env = env;
        env.enlist(this);

        this.returnTo = returnTo;
        this.template = template;
        createExPropagations(template);
    }

    @Override
    public void accept(Value v){
        env.enq(v, this);
    }

    @Override
    public Address getAddress(){
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

    public String getLabel(){
        return template.getLabel();
    }

    private ExPropagation createPropagation(FPropagation t) {
        return new ExPropagation(this, t);
    }

    protected List<ExPropagation> getPropagations(String paramName) {
        List<ExPropagation> ps = propagationsByParamName.get(paramName);
        return ps == null ? Collections.emptyList(): ps.stream().filter(p -> !p.isDone()).collect(Collectors.toList());
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
}
