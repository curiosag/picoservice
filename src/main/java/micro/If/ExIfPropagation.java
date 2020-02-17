package micro.If;

import micro.ExPropagation;

class ExIfPropagation extends ExPropagation {

    PropagationType propagationType;

    ExIfPropagation(ExIf current, IfPropagation template) {
        super(current, template);
        this.propagationType = template.propagationType;
    }

}