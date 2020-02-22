package micro;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Ex {
    private final int level = CallLevel.level;
    final Env env;
    final F template;
    protected final Ex returnTo;

    private final HashMap<String, List<ExPropagation>> propagations = new HashMap<>();
    final Map<String, Value> paramsReceived = new HashMap<>();

    public Ex(Env env, F template, Ex returnTo) {
        this.env = env;
        this.returnTo = returnTo;
        this.template = template;
        createExPropagations(template);
    }

    public Ex accept(Value v){
        env.enq(v, this);
        return this;
    };

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

                    exProps.forEach(ex -> {
                        ex.setHavingSameTarget(exProps);
                        propagations.computeIfAbsent(ex.template.nameReceived, k -> new ArrayList<>()).add(ex);
                    });

                });
    }

    protected ExPropagation createPropagation(FPropagation t) {
        return new ExPropagation(this, t);
    }

    protected List<ExPropagation> getPropagations(String paramName) {
        List<ExPropagation> result = propagations.get(paramName);
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public String toString() {
        return "(" + level + ") " + template.getLabel();
    }

    protected Value value(String name, Object value) {
        return new Value(name, value, this);
    }
}
