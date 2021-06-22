package micro;

import micro.primitives.Primitive;

public class FunctionalValueDefinition extends F {

    private final String functionalValueParam;

    public FunctionalValueDefinition(String functionalValueParam, String ... formalParams) {
        super(Primitive.nop, formalParams);
        this.functionalValueParam = functionalValueParam;
    }

    public String getFunctionalValueParam() {
        return functionalValueParam;
    }

    @Override
    public Ex createExecution(long id, _Ex returnTo, Env env) {
        return new ExFCallByFunctionalValue(env, id,this, returnTo);
    }

    public static FunctionalValueDefinition functionalValueDefinition(Env env, String functionalValueParam, String ... formalParams){
        FunctionalValueDefinition result = new FunctionalValueDefinition(functionalValueParam, formalParams);
        result.setId(env.getNextFId());
        env.addF(result);
        return result;
    }

}
