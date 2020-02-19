package micro;

import java.util.List;

public class ExPropagation {
    private List<ExPropagation> havingSameTarget;

    public final FPropagation template;
    private final Ex current;
    private Ex target;

    public ExPropagation(Ex current, FPropagation template) {
        this.current = current;
        this.template = template;
    }

    public void setTarget(Ex target) {
        this.target = target;
    }

    public Ex getTarget() {
        return target;
    }

    public void accept(Value v) {
        if (target == null) {
            target = template.target.createExecution(current.env, current);
            havingSameTarget.forEach(t -> t.setTarget(target));
        }
        target.accept(v);
    }

    void setHavingSameTarget(List<ExPropagation> havingSameTarget) {
        this.havingSameTarget = havingSameTarget;
    }

}