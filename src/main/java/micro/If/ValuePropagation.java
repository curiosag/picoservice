package micro.If;

import micro.ExPropagation;
import micro.Value;

class ValuePropagation {
    final Value value;
    final ExPropagation propagation;

    ValuePropagation(Value value, ExPropagation propagation) {
        this.value = value;
        this.propagation = propagation;
    }

}