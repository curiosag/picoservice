package micro.If;

import micro.Value;

class PendingPropagation {
    final Value value;
    final ExIfPropagation propagation;

    PendingPropagation(Value value, ExIfPropagation propagation) {
        this.value = value;
        this.propagation = propagation;
    }
}