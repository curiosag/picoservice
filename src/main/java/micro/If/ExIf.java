package micro.If;

import micro.*;

import java.util.ArrayList;
import java.util.List;

public class ExIf extends Ex {
    private Boolean condition;

    ExIf(Env env, F template, Ex returnTo) {
        super(env, template, returnTo);
    }

    @Override
    protected ExPropagation createPropagation(FPropagation t) {
        if(! (t instanceof IfPropagation))
        {
            throw new IllegalStateException();
        }
        return new ExIfPropagation(this, (IfPropagation) t);
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
        getPropagations(v.getName()).forEach(o -> {
            if (!(o instanceof ExIfPropagation)) {
                throw new IllegalStateException();
            }
            ExIfPropagation p = (ExIfPropagation) o;
            pendingPropagations.add(new PendingPropagation(v, p));
        });

        processPendingPropagations();
    }

    private void processPendingPropagations() {
         new ArrayList<>(pendingPropagations).forEach(p -> {
            if (processPendingPropagations(p)) {
                pendingPropagations.remove(p);
            }
        });
    }

    private boolean processPendingPropagations(PendingPropagation pendingPropagation) {
        ExIfPropagation p = pendingPropagation.propagation;
        Value v = value(p.template.nameToPropagate, pendingPropagation.value.get());

        switch (p.propagationType) {
            case CONDITION:
                if (condition == null) {
                    p.accept(v);
                }
                return true;

            case ON_TRUE:
                if (condition != null && condition) {
                    p.accept(v);
                }
                return condition != null;

            case ON_FALSE:
                if (condition != null && !condition) {
                    p.accept(v);
                }
                return condition != null;

            default:
                throw new IllegalStateException();
        }
    }

}
