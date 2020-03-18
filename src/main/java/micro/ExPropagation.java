package micro;

import java.util.List;

public class ExPropagation {
    private final Env env;
    private List<ExPropagation> havingSameTarget;

    private boolean done;
    public final FPropagation template;
    private final Ex from;
    private _Ex to;

    public PropagationType getPropagationType(){
        return template.propagationType;
    }

    ExPropagation(Env env, Ex from, FPropagation template) {
        this.env = env;
        this.from = from;
        this.template = template;
    }

    public void setTo(_Ex to) {
        this.to = to;
    }

    public _Ex getTo() {
        return to;
    }

    public void propagate(Value v) {
        if (to == null) {
            to = template.target.createExecution(from.env, from);
            havingSameTarget.forEach(t -> t.setTo(to));
        }
        to.accept(v);
        env.registerDone(this);
    }

    void setHavingSameTarget(List<ExPropagation> havingSameTarget) {
        this.havingSameTarget = havingSameTarget;
    }

    boolean isDone() {
        return done;
    }

    void setDone(boolean done) {
        this.done = done;
    }
}