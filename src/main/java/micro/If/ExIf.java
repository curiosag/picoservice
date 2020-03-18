package micro.If;

import micro.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static micro.PropagationType.CONDITION;

public class ExIf extends Ex {
    private Boolean condition;

    public ExIf(Env env, F template, _Ex returnTo) {
        super(env, template, returnTo);
    }

    public ExIf(Env env) {
        super(env);
    }

    @Override
    public void process(Value v) {
        registerReceived(v);

        switch (v.getName()) {
            case Names.condition:
                if (!(v.get() instanceof Boolean)) {
                    throw new IllegalStateException();
                }

                this.condition = (Boolean) v.get();
                processPendingPropagations();
                break;

            case Names.result:
                returnTo.accept(v.withSender(this));
                break;

            case Names.exception:
                returnTo.accept(v.withSender(this));
                break;

            default:
                propagate(v);
        }
    }

    private List<PendingPropagation> pendingPropagations = new ArrayList<>();

    @Override
    protected void propagate(Value v) {
        getPropagations(v.getName()).forEach(p -> pendingPropagations.add(new PendingPropagation(v, p)));

        processPendingPropagations();
    }

    private void processPendingPropagations() {
        List<PendingPropagation> pendingToPropagate = pendingPropagations.stream()
                .filter(this::canProcessPendingPropagation)
                .collect(Collectors.toList());

        pendingToPropagate.forEach(p -> {
            pendingPropagations.remove(p);
            Value v = value(p.propagation.getNameToPropagate(), p.value.get());
            p.propagation.propagate(v);
        });

    }

    private boolean canProcessPendingPropagation(PendingPropagation pendingPropagation) {
        PropagationType type = pendingPropagation.propagation.getPropagationType();

        Check.invariant(!(type.equals(CONDITION) && condition != null), "condition already set");

        switch (type) {
            case CONDITION:
                return true;

            case ON_TRUE:
                return condition != null && condition;

            case ON_FALSE:
                return condition != null && !condition;

            default:
                Check.fail("invalid case");
                return false;
        }
    }

}
