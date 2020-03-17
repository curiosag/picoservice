package micro;

import java.util.List;

public class ExPropagation {
    private List<ExPropagation> havingSameTarget;

    private boolean done;
    public final FPropagation template;
    private final Ex current;
    private _Ex target;

    public PropagationType getPropagationType(){
        return template.propagationType;
    }

    ExPropagation(Ex current, FPropagation template) {
        this.current = current;
        this.template = template;
    }

    public void setTarget(_Ex target) {
        this.target = target;
    }

    public _Ex getTarget() {
        return target;
    }

    public void propagate(Value v) {
        if (target == null) {
            target = template.target.createExecution(current.env, current);
            havingSameTarget.forEach(t -> t.setTarget(target));
        }
        target.accept(v);
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