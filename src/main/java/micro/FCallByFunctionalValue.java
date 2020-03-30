package micro;

import micro.atoms.Primitive;

public class FCallByFunctionalValue extends F {

    private String functionalValueParam;

    public FCallByFunctionalValue(Node node, String functionalValueParam, String ... formalParams) {
        super(node, Primitive.nop, formalParams);
        this.functionalValueParam = functionalValueParam;
    }

    public String getFunctionalValueParam() {
        return functionalValueParam;
    }

    @Override
    public _Ex createExecution(Node node, _Ex returnTo) {
        return new ExFCallByFunctionalValue(node, this, returnTo);
    }

}
