package micro.If;

import micro.FPropagation;
import micro._F;

class IfPropagation extends FPropagation {
    PropagationType propagationType;

    IfPropagation(PropagationType propagationType, String nameReceived, String nameToPropagate, _F target) {
        super(nameReceived, nameToPropagate, target);
        this.propagationType = propagationType;
    }
}