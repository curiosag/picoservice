package nano.implementations.nativeImpl;

import nano.ingredients.Function;
import nano.ingredients.FunctionSignature;
import nano.ingredients.Name;
import nano.ingredients.UnOp;

import java.io.Serializable;
import java.util.ArrayList;

import static nano.ingredients.Ensemble.wire;
import static nano.ingredients.FunctionSignature.functionSignature;

public class UnOps {
    public static UnOp<Boolean, Boolean> not() {
        UnOp<Boolean, Boolean> result = new UnOp<>((java.util.function.Function<Boolean, Boolean> & Serializable) v -> !v, Function.boolConverter);
        result.label("not");
        wire(result);
        return result;
    }

    public static UnOp<ArrayList<Integer>, Integer> head() {
        UnOp<ArrayList<Integer>, Integer> result = new UnOp<>((java.util.function.Function<ArrayList<Integer>, Integer> & Serializable) v -> v.isEmpty() ? null : v.get(0), Function.listConverter);
        result.label("head");
        wire(result);
        return result;
    }

    public static UnOp<ArrayList<Integer>, ArrayList<Integer>> tail() {
        UnOp<ArrayList<Integer>, ArrayList<Integer>> result = new UnOp<>((java.util.function.Function<ArrayList<Integer>, ArrayList<Integer>> & Serializable) v -> v.isEmpty() ? v : new ArrayList<>(v.subList(1, v.size())), Function.listConverter);
        result.label("tail");
        wire(result);
        return result;
    }

    public static UnOp<ArrayList<Integer>, Integer> size() {
        UnOp<ArrayList<Integer>, Integer> result = new UnOp<>((java.util.function.Function<ArrayList<Integer>, Integer> & Serializable)v -> v.isEmpty() ? null : v.size(), Function.listConverter);
        result.label("listSize");
        wire(result);
        return result;
    }

    public static <T extends Serializable> FunctionSignature<T> signature(UnOp<?, T> op) {
        FunctionSignature<T> signature = functionSignature(op);
        signature.label("FUNC " + op.address.label.toUpperCase());
        signature.propagate(Name.arg, Name.arg, op);
        return signature;
    }
}
