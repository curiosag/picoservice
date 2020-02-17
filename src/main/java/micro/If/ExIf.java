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
    public void accept(Value v) {
        registerReceived(v);

        switch (v.getName()) {
            case Names.condition:
                if (!(v.get() instanceof Boolean)) {
                    throw new IllegalStateException();
                }

                this.condition = (Boolean) v.get();
                processPendingPropagation();
                break;

            case Names.result:
                returnTo.accept(v.withSender(this));
                break;

            default:
                propagate(v);
        }
    }

    private class Pending {
        final Value value;
        final ExIfPropagation propagation;

        Pending(Value value, ExIfPropagation propagation) {
            this.value = value;
            this.propagation = propagation;
        }
    }

    private List<Pending> pending = new ArrayList<>();

    @Override
    protected void propagate(Value v) {
        getPropagations(v.getName()).forEach(o -> {
            if (!(o instanceof ExIfPropagation)) {
                throw new IllegalArgumentException();
            }
            ExIfPropagation p = (ExIfPropagation) o;
            pending.add(new Pending(v, p));
        });

        processPendingPropagation();
    }

    private void processPendingPropagation() {
        List<Pending> done = new ArrayList<>();
        // subsequent propagations may modify pending concurrently
        List<Pending> iteratePending = new ArrayList<>(pending);

        iteratePending.forEach(p -> {
            if (processPendingPropagation(p)) {
                done.add(p);
            }
        });

        pending.removeAll(done);
    }

    private boolean processPendingPropagation(Pending pending) {
        ExIfPropagation p = pending.propagation;
        Value v = value(p.template.nameToPropagate, pending.value.get());

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
