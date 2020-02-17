package micro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Ex {
    final Env env;
    final F template;
    protected final Ex returnTo;

    private HashMap<String, List<ExPropagation>> propagations = new HashMap<>();

    final List<Value> paramsReceived = new ArrayList<>();

    public Ex(Env env, F template, Ex returnTo) {
        this.env = env;
        this.returnTo = returnTo;
        this.template = template;
        createExPropagations(template);
    }

    public abstract void accept(Value v);

    protected abstract void propagate(Value v);

    protected void registerReceived(Value v) {
        env.debug(template.getLabel() + " got " + v.toString() + " from " + v.getSender());

        if (template.formalParameters.contains(v.getName())) {
            paramsReceived.add(v);
        }
    }

    void createExPropagations(F template) {
        template.getTargetFunctionsToPropagations().forEach(
                (targetFunc, templateProps) -> {

                    List<ExPropagation> exProps = templateProps.stream()
                            .map(t -> createPropagation(t))
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
        return template.getLabel() != null ? template.getLabel() : "no name";
    }

    protected Value value(String name, Object value) {
        return new Value(name, value, this);
    }
}
