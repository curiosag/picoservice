package micro.If;

import micro.F;
import micro.FPropagation;

class IfPropagation extends FPropagation {
        PropagationType propagationType;

        IfPropagation(PropagationType propagationType, String nameReceived, String nameToPropagate, F target) {
            super(nameReceived, nameToPropagate, target);
            this.propagationType = propagationType;
        }
    }