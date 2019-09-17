package miso.implementations;

import miso.ingredients.Function;
import miso.ingredients.FunctionSignature;

import java.util.List;

public class Implementation<T> {

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
