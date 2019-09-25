package miso.ingredients.nativeImpl;

import miso.ingredients.Actresses;
import miso.ingredients.BinOp;

import java.util.ArrayList;
import java.util.List;

import static miso.ingredients.Function.intConverter;
import static miso.ingredients.Function.listConverter;

public class ListBinOps {

    public static BinOp<List<Integer>, List<Integer>, Boolean> eq() {
        BinOp<List<Integer>, List<Integer>, Boolean> result = new BinOp<>(List::equals, listConverter, listConverter);
        result.label("equal");
        Actresses.wire(result);
        return result;
    }

    public static miso.ingredients.BinOp<List<Integer>, List<Integer>, List<Integer>> concat() {
        BinOp<List<Integer>, List<Integer>, List<Integer>> result = new BinOp<>((n1, n2) -> {
            List<Integer> conc = new ArrayList<>(n1);
            conc.addAll(n2);
            return conc;
        }, listConverter, listConverter);
        result.label("conc");
        Actresses.wire(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, List<Integer>, List<Integer>> cons() {
        BinOp<Integer, List<Integer>, List<Integer>> result = new BinOp<>((n1, n2) -> {
            List<Integer> cons = new ArrayList<>();
            cons.add(n1);
            cons.addAll(n2);
            return cons;
        }, intConverter, listConverter);
        result.label("cons");
        Actresses.wire(result);
        return result;
    }
}
