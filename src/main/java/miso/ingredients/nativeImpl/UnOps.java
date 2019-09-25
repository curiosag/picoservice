package miso.ingredients.nativeImpl;

import miso.ingredients.*;

import java.util.List;
import java.util.Objects;

import static miso.ingredients.Actresses.wire;
import static miso.ingredients.FunctionSignature.functionSignature;

public class UnOps {
    public static UnOp<Boolean, Boolean> not() {
        UnOp<Boolean, Boolean> result = new UnOp<>(v -> !v, Function.boolConverter);
        result.label("not");
        wire(result);
        return result;
    }

    public static UnOp<Object, Boolean> isNull() {
        UnOp<Object, Boolean> result = new UnOp<>(Objects::isNull, v -> v);
        result.label("isNull");
        wire(result);
        return result;
    }

    public static UnOp<List<Integer>, Integer> head() {
        UnOp<List<Integer>, Integer> result = new UnOp<>(v -> v.isEmpty() ? null : v.get(0), Function.listConverter);
        result.label("head");
        wire(result);
        return result;
    }

    public static UnOp<List<Integer>, List<Integer>> tail() {
        UnOp<List<Integer>, List<Integer>> result = new UnOp<>(v -> v.isEmpty() ? v : v.subList(1, v.size()), Function.listConverter);
        result.label("tail");
        wire(result);
        return result;
    }

    public static UnOp<List<Integer>, Integer> size() {
        UnOp<List<Integer>, Integer> result = new UnOp<>(v -> v.isEmpty() ? null : v.size(), Function.listConverter);
        result.label("listSize");
        wire(result);
        return result;
    }

    public static <T> FunctionSignature<T> signature(UnOp<?, T> op) {
        FunctionSignature<T> signature = functionSignature(op);
        signature.label("FUNC " + op.address.label.toUpperCase());
        signature.propagate(Name.arg, Name.arg, op);
        return signature;
    }
}
