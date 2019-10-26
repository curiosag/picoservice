package nano.implementations;

import nano.ingredients.Function;
import nano.ingredients.FunctionSignature;

import java.io.Serializable;
import java.util.List;

public class Implementation<T extends Serializable> {

    private final FunctionSignature<T> impl;
    private final List<Function> functions;

    public Implementation(FunctionSignature<T> impl, List<Function> functions) {
        this.impl = impl;
        this.functions = functions;
    }

    public FunctionSignature<T> get(){
        return impl;
    }

    public List<Function> getFunctions(){
        return functions;
    }

}
