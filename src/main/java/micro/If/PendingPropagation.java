package micro.If;

import micro.ExPropagation;
import micro.Value;

class PendingPropagation {
    final Value value;
    final ExPropagation propagation;

    PendingPropagation(Value value, ExPropagation propagation) {
        this.value = value;
        this.propagation = propagation;
    }
}