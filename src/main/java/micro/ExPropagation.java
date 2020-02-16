package micro;

import java.util.List;

class ExPropagation {
    private List<ExPropagation> havingSameTarget;

    final FPropagation template;
    private final Ex current;
    private Ex target;

    ExPropagation(Ex current, FPropagation template) {
        this.current = current;
        this.template = template;
    }

    public void setTarget(Ex target) {
        this.target = target;
    }

    public Ex getTarget() {
        return target;
    }

    void accept(Value v) {
        if (target == null) {
            target = template.target.newExecution(current.env, current);
            havingSameTarget.forEach(t -> t.setTarget(target));
        }
        target.accept(v);
    }

    void setHavingSameTarget(List<ExPropagation> havingSameTarget) {
        this.havingSameTarget = havingSameTarget;
    }

}