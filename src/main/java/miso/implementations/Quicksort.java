package miso.implementations;

import miso.ingredients.*;
import miso.ingredients.nativeImpl.ListBinOps;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static miso.implementations.Filter.filterSignatureJava;
import static miso.ingredients.ConditionalPropagation.conditionalPropagation;
import static miso.ingredients.FunctionCall.functionCall;
import static miso.ingredients.FunctionSignature.functionSignature;
import static miso.ingredients.Iff.iffList;
import static miso.ingredients.PartialFunctionApplication.partialApplication;
import static miso.ingredients.nativeImpl.BinOps.gteq;
import static miso.ingredients.nativeImpl.BinOps.lt;
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
                quicksort(filter(tail, i -> lt(i, head))) + head :: quicksort(filter(tail, i -> gteq(i, head)))
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

        // quicksort(filter(tail, i -> lt(i, head))) + head :: quicksort(filter(tail, i -> gteq(i, head))))
        // +
        Function<List<Integer>> concat = concat().returnTo(iff, Name.onFalse);
        // ::
        Function<List<Integer>> cons = cons().returnTo(concat, Name.rightArg);
        iff.propagateOnFalse(Name.head, Name.leftArg, cons);
        // 2 times quicksort(filter(...))
        Function<List<Integer>> qsortReCallLeft = functionCall(qsortSignature).returnTo(concat, Name.leftArg);
        Function<List<Integer>> qsortReCallRight = functionCall(qsortSignature).returnTo(cons, Name.rightArg);

        FunctionSignature<List<Integer>> filterSignatureLeft = filterSignatureJava().get();
        FunctionSignature<List<Integer>> filterSignatureRight = filterSignatureJava().get();

        // function lt(a, b) = a < b;
        // filter(tail, i -> lt(i, head))
        BinOp<Integer, Integer, Boolean> lt = lt();
        Function<Boolean> ltPredicate = partialApplication(lt, Name.rightArg)
                .propagate(Name.arg, Name.leftArg, lt)
                .propagate(Name.rightArg, Name.rightArg, lt);

        Function<List<Integer>> filterCallLeft =
                functionCall(filterSignatureLeft)
                        .constant(Name.predicate, ltPredicate)
                        .returnTo(qsortReCallLeft, Name.list);

        Function propagateLeftOnCondition = conditionalPropagation()
                .addPriorityParam(Name.rightArg)
                .propagate(Name.rightArg, Name.rightArg, ltPredicate)
                .propagate(Name.list, Name.list, filterCallLeft);

        iff.propagateOnFalse(Name.head, Name.rightArg, propagateLeftOnCondition);
        iff.propagateOnFalse(Name.tail, Name.list, propagateLeftOnCondition);

        // function gteq(a, b) = a >= b;
        // filter(filter(tail, i -> gteq(i, head))
        Function<Boolean> gteq = gteq();
        Function<Boolean> gtEqPredicate = partialApplication(gteq, Name.rightArg)
                .propagate(Name.arg, Name.leftArg, gteq)
                .propagate(Name.rightArg, Name.rightArg, gteq);

        Function<List<Integer>> filterCallRight =
                functionCall(filterSignatureRight)
                        .constant(Name.predicate, gtEqPredicate)
                        .returnTo(qsortReCallRight, Name.list);

        Function propagateRightOnCondition = conditionalPropagation()
                .addPriorityParam(Name.rightArg)
                .propagate(Name.rightArg, Name.rightArg, gtEqPredicate)
                .propagate(Name.list, Name.list, filterCallRight);

        iff.propagateOnFalse(Name.head, Name.rightArg, propagateRightOnCondition);
        iff.propagateOnFalse(Name.tail, Name.list, propagateRightOnCondition);

        iff.onReturnOnFalseSend(Name.removePartialAppValues, null, ltPredicate);
        iff.onReturnOnFalseSend(Name.removePartialAppValues, null, gtEqPredicate);
        iff.onReturnOnFalseSend(Name.removeState, null, propagateLeftOnCondition);
        iff.onReturnOnFalseSend(Name.removeState, null, propagateRightOnCondition);

        qsortSignature.onReturnSend(Name.removeStatesForExecution, null, filterSignatureRight);
        qsortSignature.onReturnSend(Name.removeStatesForExecution, null, filterSignatureLeft);

        iff.label("iff");
        qsortSignature.label("QSORT");
        filterSignatureLeft.label("FILTER LEFT");
        filterSignatureRight.label("FILTER RIGHT");
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
