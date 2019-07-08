package miso.ingredients;

import miso.Message;
import miso.Name;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BinOp<T, V> extends Func {

    private final BiFunction<T, T, V> op;
    private final Function<Object, T> converter;

    private String leftKey;
    private String rightKey;

    public BinOp(BiFunction<T, T, V> op, Function<Object, T> converter) {
        this.op = op;
        this.converter = converter;
        argKeys(Name.leftArg, Name.rightArg);
    }

    public BinOp<T,V> argKeys(String leftKey, String rightKey){
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        paramsRequired(leftKey, rightKey);
        return this;
    }

    @Override
    protected Message getNext() {
        return getCurrent()
                .map(m -> {
                    try {
                        T left = converter.apply(m.get(leftKey));
                        T right = converter.apply(m.get(rightKey));
                        return Message.of(this, resultKey, op.apply(left, right));
                    } catch (Exception e) {
                        return Message.of(this, Name.error, e.toString());
                    }
                })
                .orElseGet(NULL);

    }
}
