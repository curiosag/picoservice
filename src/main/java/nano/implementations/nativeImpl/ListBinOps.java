package nano.implementations.nativeImpl;

import nano.ingredients.Ensemble;
import nano.ingredients.BinOp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static nano.ingredients.Function.intConverter;
import static nano.ingredients.Function.listConverter;

public class ListBinOps {

    public static BinOp<ArrayList<Integer>, ArrayList<Integer>, Boolean> eq() {
        BinOp<ArrayList<Integer>, ArrayList<Integer>, Boolean> result = new BinOp<>((BiFunction<ArrayList<Integer>, ArrayList<Integer>, Boolean> & Serializable) List::equals, listConverter, listConverter);
        result.label("equal");
        Ensemble.attachActor(result);
        return result;
    }

    public static nano.ingredients.BinOp<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>> concat() {
        BinOp<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>> result = new BinOp<>((BiFunction<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>> & Serializable)(n1, n2) -> {
            ArrayList<Integer> conc = new ArrayList<>(n1);
            conc.addAll(n2);
            return conc;
        }, listConverter, listConverter);
        result.label("conc");
        Ensemble.attachActor(result);
        return result;
    }

    public static nano.ingredients.BinOp<Integer, ArrayList<Integer>, ArrayList<Integer>> cons() {
        BinOp<Integer, ArrayList<Integer>, ArrayList<Integer>> result = new BinOp<>((BiFunction<Integer, ArrayList<Integer>, ArrayList<Integer>> & Serializable)(n1, n2) -> {
            ArrayList<Integer> cons = new ArrayList<>();
            cons.add(n1);
            cons.addAll(n2);
            return cons;
        }, intConverter, listConverter);
        result.label("cons");
        Ensemble.attachActor(result);
        return result;
    }
}
