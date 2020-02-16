package micro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Ex {
    final Env env;
    private final F template;
    private Ex returnTo;

    private HashMap<String, List<ExPropagation>> propagations = new HashMap<>();
    private List<Value> paramsReceived = new ArrayList<>();

    Ex(Env env, F template, Ex returnTo) {
        this.env = env;
        this.template = template;
        this.returnTo = returnTo;
        setupPropagations(template);
    }

    Ex() {
        template = null;
        env = null;
    }

    private void setupPropagations(F template) {
        template.getTargetFunctionsToPropagations().forEach((targetFunc, templateProps) -> {
            List<ExPropagation> exProps = templateProps.stream()
                    .map(t -> new ExPropagation(this, t))
                    .collect(Collectors.toList());

            exProps.forEach(ex -> {
                ex.setHavingSameTarget(exProps);
                propagations.computeIfAbsent(ex.template.nameReceived, k -> new ArrayList<>()).add(ex);
            });
        });
    }

    public void accept(Value v) {
        env.debug(template.getLabel() + " got " + v.toString() + " from " + v.getSender());

        if (template.formalParameters.contains(v.getName())) {
            paramsReceived.add(v);
        }

        if (template.getAtom() != null && paramsReceived.size() == template.numParams()) {
            if (template.getAtom().isSideEffect()) {
                runSideEffect();
            } else {
                runFunction();
            }
        }

        if (Names.result.equals(v.getName())) {
            returnTo.accept(value(template.returnAs, v.get()));
        }

        propagate(v);
    }

    private void propagate(Value v) {
        getPropagations(v.getName()).ifPresent(propagations ->
                propagations.forEach(p -> p.accept(value(p.template.nameToPropagate, v.get())))
        );
    }

    private Optional<List<ExPropagation>> getPropagations(String paramName) {
        return Optional.ofNullable(propagations.get(paramName));
    }

    private void runSideEffect() {
        try {
            template.getAtom().execute(paramsReceived);
        } catch (Exception e) {
            env.log(e.getMessage());
        }
    }

    private void runFunction() {
        try {
            returnTo.accept(value(template.returnAs, template.getAtom().execute(paramsReceived)));
        } catch (Exception e) {
            returnTo.accept(value(Names.exception, e));
        }
    }

    private Value value(String name, Object value) {
        return new Value(name, value, this);
    }

    @Override
    public String toString() {
        return template.getLabel() != null ? template.getLabel() : "no name";
    }
}
