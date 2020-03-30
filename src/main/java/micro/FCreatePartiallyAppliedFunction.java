package micro;

import micro.atoms.Primitive;

public class FCreatePartiallyAppliedFunction extends F {

    private F baseFunction;

    public FCreatePartiallyAppliedFunction(Node n, F baseFunction, String... partialParams) {
        super(n, Primitive.nop, partialParams);
        this.baseFunction = baseFunction;
        setPrimitive(parameters -> new PartiallyAppliedFunction(this.baseFunction, parameters.values()));
    }

}