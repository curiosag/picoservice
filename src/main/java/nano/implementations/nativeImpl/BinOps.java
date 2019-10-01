package nano.implementations.nativeImpl;

import nano.ingredients.BinOp;
import nano.ingredients.Function;
import nano.ingredients.FunctionSignature;
import nano.ingredients.Name;

import java.io.Serializable;
import java.util.function.BiFunction;

import static nano.ingredients.Ensemble.wire;
import static nano.ingredients.FunctionSignature.functionSignature;

public class BinOps {

    public static nano.ingredients.BinOp<Boolean, Boolean, Boolean> and() {
        BinOp<Boolean, Boolean, Boolean> result = new BinOp<>((BiFunction<Boolean, Boolean, Boolean> & Serializable)(n1, n2) -> n1 && n2, Function.boolConverter, Function.boolConverter);
        result.label("&");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Boolean> eq() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((BiFunction<Integer, Integer, Boolean> & Serializable)(n1, n2) -> n1 == n2, Function.intConverter, Function.intConverter);
        result.label("==");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Boolean> lt() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((BiFunction<Integer, Integer, Boolean> & Serializable)(n1, n2) -> n1 < n2, Function.intConverter, Function.intConverter);
        result.label("<");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Boolean> gt() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((BiFunction<Integer, Integer, Boolean> & Serializable)(n1, n2) -> n1 > n2, Function.intConverter, Function.intConverter);
        result.label(">");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Boolean> lteq() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((BiFunction<Integer, Integer, Boolean> & Serializable)(n1, n2) -> n1 <= n2, Function.intConverter, Function.intConverter);
        result.label("<=");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Boolean> gteq() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((BiFunction<Integer, Integer, Boolean> & Serializable)(n1, n2) -> n1 >= n2, Function.intConverter, Function.intConverter);
        result.label(">=");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Integer> add() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((BiFunction<Integer, Integer, Integer> & Serializable) (n1, n2) -> n1 + n2, Function.intConverter, Function.intConverter);
        result.label("+");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Integer> sub() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((BiFunction<Integer, Integer, Integer> & Serializable) (n1, n2) -> n1 - n2, Function.intConverter, Function.intConverter);
        result.label("-");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Integer> mul() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((BiFunction<Integer, Integer, Integer> & Serializable) (n1, n2) -> n1 * n2, Function.intConverter, Function.intConverter);
        result.label("*");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Integer> div() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((BiFunction<Integer, Integer, Integer> & Serializable) (n1, n2) -> n1 / n2, Function.intConverter, Function.intConverter);
        result.label("div");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Integer> mod() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((BiFunction<Integer, Integer, Integer> & Serializable) (n1, n2) -> n1 % n2, Function.intConverter, Function.intConverter);
        result.label("mod");
        wire(result);
        return result;
    }

    public static <T extends Serializable> FunctionSignature<T> signature(BinOp<?,?, T> op) {
        FunctionSignature<T> signature = functionSignature(op);
        signature.label("FUNC: " + op.address.label.toUpperCase());

        signature.propagate(Name.leftArg, Name.leftArg, op);
        signature.propagate(Name.rightArg, Name.rightArg, op);

        return signature;
    }

}
