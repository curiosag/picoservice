package miso.ingredients;

import java.util.ArrayList;
import java.util.List;

import static miso.ingredients.Function.intConverter;
import static miso.ingredients.Function.listConverter;

public class ListBinOp {


    public static BinOp<List<Integer>, List<Integer>, Boolean> eq() {
        BinOp<List<Integer>, List<Integer>, Boolean> result = new BinOp<>(List::equals, listConverter, listConverter);
        Actress.start(result);
        return result;
    }

    public static miso.ingredients.BinOp<Integer, List<Integer>, List<Integer>> cons() {
        BinOp<Integer, List<Integer>, List<Integer>> result = new BinOp<>((n1, n2) -> {
            n2.add(0, n1);
            ArrayList<Integer> integers = new ArrayList<>();
            integers.add(n1);
            integers.addAll(n2);
            return integers;
        }, intConverter, listConverter);
        Actress.start(result);
        return result;
    }
}
