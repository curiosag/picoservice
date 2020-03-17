package micro.If;

import micro.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExIf extends Ex {
    private Boolean condition;

    ExIf(Env env, F template, Ex returnTo) {
        super(env, template, returnTo);
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

        if (pendingToPropagate.size() > 1) {
            throw new IllegalStateException();
        }

        pendingToPropagate.forEach(p -> {
            pendingPropagations.remove(p);
            Value v = value(p.propagation.template.nameToPropagate, p.value.get());
            p.propagation.accept(v);
        });

    }

    private boolean canProcessPendingPropagation(PendingPropagation pendingPropagation) {
        switch (pendingPropagation.propagation.getPropagationType()) {
            case CONDITION:
                if (condition != null) {
                    throw new IllegalStateException();
                }
                return true;

            case ON_TRUE:
                return condition != null && condition;

            case ON_FALSE:
                return condition != null && !condition;

            default:
                throw new IllegalStateException();
        }
    }

}
