package micro.If;

import micro.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static micro.PropagationType.*;

public class ExIf extends Ex {
    private Boolean condition;
    private List<ValuePropagation> conditionalPropagations = new ArrayList<>();

    ExIf(Env env, F template, _Ex returnTo) {
        super(env, template, returnTo);
    }

    public ExIf(Env env) {
        super(env);
    }

    @Override
    public void process(Value v) {
        Check.invariant(!(Names.result.equals(v.getName()) || Names.exception.equals(v.getName())), "result and exception expected to be processed in base class");

        if (Names.condition.equals(v.getName())) {
            Check.invariant(v.get() instanceof Boolean, "condition value must be boolean");
            this.condition = (Boolean) v.get();
            processConditionalPropagations();
        } else {
            propagate(v);
        }
    }

    private void propagate(Value v) {
        getPropagations(v.getName()).forEach(p -> {
            if (p.getPropagationType().in(FALSE_BRANCH, TRUE_BRANCH)) {
                conditionalPropagations.add(new ValuePropagation(v, p));
            } else {
                p.propagate(value(p.getNameToPropagate(), v.get()));
            }
        });

        processConditionalPropagations();
    }

    private void processConditionalPropagations() {
        List<ValuePropagation> conditionalToPropagate = conditionalPropagations.stream()
                .filter(this::canProcessPropagation)
                .collect(Collectors.toList());

        conditionalToPropagate.forEach(p -> {
            conditionalPropagations.remove(p);
            Value v = value(p.propagation.getNameToPropagate(), p.value.get());
            p.propagation.propagate(v);
        });

    }

    private boolean canProcessPropagation(ValuePropagation valuePropagation) {
        PropagationType type = valuePropagation.propagation.getPropagationType();

        switch (type) {

            case TRUE_BRANCH:
                return condition != null && condition;

            case FALSE_BRANCH:
                return condition != null && !condition;

            default:
                Check.fail("invalid case");
                return false;
        }
    }

}
