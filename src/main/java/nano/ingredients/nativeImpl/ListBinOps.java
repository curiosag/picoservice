package nano.ingredients.nativeImpl;

import nano.ingredients.Actresses;
import nano.ingredients.BinOp;

import java.util.ArrayList;
import java.util.List;

import static nano.ingredients.Function.intConverter;
import static nano.ingredients.Function.listConverter;

public class ListBinOps {

    public static BinOp<List<Integer>, List<Integer>, Boolean> eq() {
        BinOp<List<Integer>, List<Integer>, Boolean> result = new BinOp<>(List::equals, listConverter, listConverter);
        result.label("equal");
        Actresses.wire(result);
        return result;
    }

    public static nano.ingredients.BinOp<List<Integer>, List<Integer>, List<Integer>> concat() {
        BinOp<List<Integer>, List<Integer>, List<Integer>> result = new BinOp<>((n1, n2) -> {
            List<Integer> conc = new ArrayList<>(n1);
            conc.addAll(n2);
            return conc;
        }, listConverter, listConverter);
        result.label("conc");
        Actresses.wire(result);
        return result;
    }

    public static nano.ingredients.BinOp<Integer, List<Integer>, List<Integer>> cons() {
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
