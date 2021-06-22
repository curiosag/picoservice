package micro;

import micro.primitives.Primitive;

public class FCreatePartiallyAppliedFunction extends F {

    private F baseFunction;

    public FCreatePartiallyAppliedFunction(F baseFunction, String... partialParams) {
        super(Primitive.nop, partialParams);
        this.baseFunction = baseFunction;
        setPrimitive(parameters -> new PartiallyAppliedFunction(this.baseFunction, parameters.values()));
    }

    public static FCreatePartiallyAppliedFunction fCreatePartiallyAppliedFunction(Env env, F baseFunction, String... partialParams){
        var result = new FCreatePartiallyAppliedFunction(baseFunction, partialParams);
        env.register(result);
        return result;
    }

}