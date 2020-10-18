package micro.If;

import micro.FPropagation;
import micro.Value;

class ValuePropagation {
    final Value value;
    final FPropagation propagation;

    ValuePropagation(Value value, FPropagation propagation) {
        this.value = value;
        this.propagation = propagation;
    }

    @Override
    public String toString() {
        return "{\"ValuePropagation\":{" +
                "\"value\":" + value +
                ", \"fPropagation\":" + propagation +
                "}}";
    }
}