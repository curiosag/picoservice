package miso.ingredients.nativeImpl;

import miso.ingredients.BinOp;
import miso.ingredients.Function;
import miso.ingredients.FunctionSignature;
import miso.ingredients.Name;

import static miso.ingredients.Actresses.wire;
import static miso.ingredients.FunctionSignature.functionSignature;

public class BinOps {

    public static miso.ingredients.BinOp<Boolean, Boolean, Boolean> and() {
        BinOp<Boolean, Boolean, Boolean> result = new BinOp<>((n1, n2) -> n1 && n2, Function.boolConverter, Function.boolConverter);
        result.label("&");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Boolean> eq() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 == n2, Function.intConverter, Function.intConverter);
        result.label("==");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Boolean> lt() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 < n2, Function.intConverter, Function.intConverter);
        result.label("<");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Boolean> gt() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 > n2, Function.intConverter, Function.intConverter);
        result.label(">");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Boolean> lteq() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 <= n2, Function.intConverter, Function.intConverter);
        result.label("<=");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Boolean> gteq() {
        BinOp<Integer, Integer, Boolean> result = new BinOp<>((n1, n2) -> n1 >= n2, Function.intConverter, Function.intConverter);
        result.label(">=");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Integer> add() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((n1, n2) -> n1 + n2, Function.intConverter, Function.intConverter);
        result.label("+");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Integer> sub() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((n1, n2) -> n1 - n2, Function.intConverter, Function.intConverter);
        result.label("-");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Integer> mul() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((n1, n2) -> n1 * n2, Function.intConverter, Function.intConverter);
        result.label("*");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Integer> div() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((n1, n2) -> n1 / n2, Function.intConverter, Function.intConverter);
        result.label("div");
        wire(result);
        return result;
    }

    public static BinOp<Integer, Integer, Integer> mod() {
        BinOp<Integer, Integer, Integer> result = new BinOp<>((n1, n2) -> n1 % n2, Function.intConverter, Function.intConverter);
        result.label("mod");
        wire(result);
        return result;
    }

    public static <T> FunctionSignature<T> signature(BinOp<?,?, T> op) {
        FunctionSignature<T> signature = functionSignature(op);
        signature.label("FUNC: " + op.address.label.toUpperCase());

        signature.propagate(Name.leftArg, Name.leftArg, op);
        signature.propagate(Name.rightArg, Name.rightArg, op);

        return signature;
    }

}
