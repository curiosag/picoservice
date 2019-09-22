package miso.implementations;

import miso.ingredients.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
            if (firstList != null) {
                execFilter(m, firstList);
                firstList = null;
            }
        }

    }

    private final String LT = "LT";
    private final String GTEQ = "GTEQ";

    private void execFilter(Message m, List<Integer> list) {
        Integer rightArg = getRightArg(m.origin);
        String op = null;
        if (app.address.label.contains(GTEQ)) {
            op = GTEQ;
        } else if (app.address.label.contains(LT)) {
            op = LT;
        }
        if (op == null || list == null) {
            throw new IllegalStateException();
        }
        List<Integer> result = new ArrayList<>();
        for (Integer i : list) {
            if (i == null ) {
                throw new IllegalStateException();
            }

            if ((LT.equals(op) && i < rightArg) || (GTEQ.equals(op) && i >= rightArg)) {
                result.add(i);
            }

        }

        debug(String.format("filtering %s for %s r=%d (from %s) -> %s", list.toString(), app.address.label, rightArg, getP(app.partialAppValues), result.toString()));
        removeState(m.origin);
        returnResult(result, m.origin);
    }

    private String getP(Map<PartialAppMatcher, Stack<Map<String, Object>>> pApp) {
        StringBuilder result = new StringBuilder();
        pApp.forEach((k, v) -> v.forEach(i -> i.forEach((kk, vv) -> result.append(vv.toString()).append(","))));
        return result.toString();
    }

    private Integer getRightArg(Origin o) {
        Map values = app.getPartialAppValues(o);
        return (Integer) values.get(Name.rightArg);
    }

}
