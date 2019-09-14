package miso.implementations;

import miso.ingredients.*;
import miso.ingredients.nativeImpl.ListBinOps;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static miso.implementations.Filter.getFilterSignature;
import static miso.ingredients.FunctionCall.functionCall;
import static miso.ingredients.FunctionSignature.functionSignature;
import static miso.ingredients.Iff.iffList;
import static miso.ingredients.PartialFunctionApplication.partialApplication;
import static miso.ingredients.nativeImpl.BinOps.*;
import static miso.ingredients.nativeImpl.ListBinOps.concat;
import static miso.ingredients.nativeImpl.ListBinOps.cons;
import static miso.ingredients.nativeImpl.UnOps.head;
import static miso.ingredients.nativeImpl.UnOps.tail;

public class Quicksort {

    public static FunctionSignature<List<Integer>> getQuicksortSignature() {

    /*

    function lt(a, b) = a < b;
    function gteq(a, b) = a >= b;
    function filter(list, predicate) =...

    function quicksort(list) =
            if (list == [])
                []
            else
                let head = head(list)
                let tail = tail(list)
                quicksort(filter(tail, i -> lt(i, head))) + head :: quicksort(filter(filter(tail, i -> gteq(i, head))))
    */
        //----------------------------------------------------------------------------------------------
    /*    function quicksort(list) =
            if (list == [])
                []
     */
        Iff<List<Integer>> iff = iffList();
        FunctionSignature<List<Integer>> qsortSignature = functionSignature(iff);
        qsortSignature.propagate(Name.list, Name.list, iff);
        iff.constant(Name.onTrue, emptyList());
        Function<Boolean> listEq = ListBinOps.eq().returnTo(iff, Name.condition).constant(Name.rightArg, emptyList());
        iff.propagate(Name.list, Name.leftArg, listEq);

        // else
        //      let head = head(list);
        //      let tail = tail(list);
        Function<Integer> head = head().returnTo(iff, Name.head);
        Function<List<Integer>> tail = tail().returnTo(iff, Name.tail);
        iff.propagateOnFalse(Name.list, Name.arg, head);
        iff.propagateOnFalse(Name.list, Name.arg, tail);

        // quicksort(filter(tail, i -> lt(i, head))) + head :: quicksort(filter(filter(tail, i -> gteq(i, head))))
        // +
        Function<List<Integer>> concat = concat().returnTo(iff, Name.onFalse);
        // ::
        Function<List<Integer>> cons = cons().returnTo(concat, Name.rightArg);
        iff.propagateOnFalse(Name.head, Name.leftArg, cons);
        // 2 times quicksort(filter(...))
        Function<List<Integer>> qsortReCallLeft = functionCall(qsortSignature).returnTo(concat, Name.leftArg);
        Function<List<Integer>> qsortReCallRight = functionCall(qsortSignature).returnTo(cons, Name.rightArg);
        FunctionSignature<List<Integer>> filterSignature = getFilterSignature();

        // function lt(a, b) = a < b;
        // filter(tail, i -> lt(i, head))
        BinOp<Integer, Integer, Boolean> lt = lt();
        Function<Boolean> ltPredicate = partialApplication(lt, list(Name.rightArg))
                .propagate(Name.arg, Name.leftArg, lt)
                .propagate(Name.rightArg, Name.rightArg, lt);

        Function<List<Integer>> filterCallLeft =
                functionCall(filterSignature)
                        .constant(Name.predicate, ltPredicate)
                        .returnTo(qsortReCallLeft, Name.list);

        iff.propagateOnFalse(Name.head, Name.rightArg, ltPredicate);
        iff.propagateOnFalse(Name.tail, Name.list, filterCallLeft);

        // function gteq(a, b) = a >= b;
        // filter(filter(tail, i -> gteq(i, head))
        Function<Boolean> gteq = gteq();
        Function<Boolean> gtEqPredicate = partialApplication(gteq, list(Name.rightArg))
                .propagate(Name.arg, Name.leftArg, gteq)
                .propagate(Name.rightArg, Name.rightArg, gteq);

        Function<List<Integer>> filterCallRight =
                functionCall(filterSignature)
                        .constant(Name.predicate, gtEqPredicate)
                        .returnTo(qsortReCallRight, Name.list);
        iff.propagateOnFalse(Name.head, Name.rightArg, gtEqPredicate);
        iff.propagateOnFalse(Name.tail, Name.list, filterCallRight);

        iff.onReturnSend(Name.popPartialAppValues, null, ltPredicate);
        iff.onReturnSend(Name.popPartialAppValues, null, gtEqPredicate);

        iff.label("iff");
        qsortSignature.label("QSORT");
        filterSignature.label("FILTER");
        qsortReCallLeft.label("qsortReCallLeft");
        qsortReCallRight.label("qsortReCallRight");
        ltPredicate.label("LT(l,r)");
        gtEqPredicate.label("GTEQ(l,r)");
        filterCallLeft.label("filterCallLeft");
        filterCallRight.label("filterCallRight");

        return qsortSignature;
    }

    private static List<String> list(String... p) {
        return asList(p);
    }

    private static List<Integer> emptyList() {
        return Collections.emptyList();
    }
}
