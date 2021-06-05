package micro;

import micro.primitives.Primitive;

public class FunctionalValueDefinition extends F {

    private final String functionalValueParam;

    public FunctionalValueDefinition(Env env, String functionalValueParam, String ... formalParams) {
        super(env, Primitive.nop, formalParams);
        this.functionalValueParam = functionalValueParam;
    }

    public String getFunctionalValueParam() {
        return functionalValueParam;
    }

    @Override
    public Ex createExecution(long id, _Ex returnTo) {
        return new ExFCallByFunctionalValue(this.env, id,this, returnTo);
    }

}
