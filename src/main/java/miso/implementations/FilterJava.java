package miso.implementations;

import miso.ingredients.*;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilterJava extends FunctionSignature<List<Integer>> {

    private static List<Integer> oogleInt(List<Integer> i) {
        return i;
    }

    FilterJava() {
        super(new UnOp<>(FilterJava::oogleInt, Function.listConverter));
        returnTo(body.returnTo, body.returnKey);
    }

    PartialFunctionApplication app;
    List<Integer> firstList;

    @Override
    public void process(Message m) {
        super.process(m);

        if (m.key.equals(Name.list)) {
            List<Integer> list = (List<Integer>) m.value;
            if (app == null) {
                firstList = list;
            } else {
                execFilter(m, list);
            }

        }
        if (m.key.equals(Name.predicate)) {
            app = (PartialFunctionApplication) m.value;
            if (firstList != null)
            {
                execFilter(m, firstList);
                firstList = null;
            }
        }

    }

    private void execFilter(Message m, List<Integer> list) {
        Integer rightArg = getRightArg(m.origin);
        Predicate<Integer> op;
        if (app.address.label.contains("GTEQ")) {
            op = i -> i >= rightArg;
        } else {
            op = i -> i < rightArg;
        }
        List<Integer> result = list.stream().filter(op).collect(Collectors.toList());
        removeState(m.origin);
        returnResult(result, m.origin);
    }

    private Integer getRightArg(Origin o) {
        Map<String, Object> values = app.getPartialAppValues(o);
        return (Integer) values.get(Name.rightArg);
    }

}
