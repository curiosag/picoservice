package nano.implementations;

import nano.implementations.nativeImpl.ListBinOps;
import nano.ingredients.*;

import java.util.ArrayList;

import static nano.implementations.Filter.filterSignature;
import static nano.implementations.nativeImpl.BinOps.gteq;
import static nano.implementations.nativeImpl.BinOps.lt;
import static nano.implementations.nativeImpl.ListBinOps.concat;
import static nano.implementations.nativeImpl.ListBinOps.cons;
import static nano.implementations.nativeImpl.UnOps.head;
import static nano.implementations.nativeImpl.UnOps.tail;
import static nano.ingredients.FunctionCall.functionCall;
import static nano.ingredients.FunctionSignature.functionSignature;
import static nano.ingredients.Iff.iffList;
import static nano.ingredients.PartialFunctionApplication.partialApplication;
import static nano.ingredients.PrioritizedPropagation.conditionalPropagation;

public class Quicksort {

    public static FunctionSignature<ArrayList<Integer>> getQuicksortSignature() {

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
        Iff<ArrayList<Integer>> iff = iffList();
        FunctionSignature<ArrayList<Integer>> qsortSignature = functionSignature(iff);
        qsortSignature.propagate(Name.list, Name.list, iff);
        iff.constant(Name.onTrue, emptyList());
        Function<Boolean> listEq = ListBinOps.eq().returnTo(iff, Name.condition).constant(Name.rightArg, emptyList());
        iff.propagate(Name.list, Name.leftArg, listEq);

        // else
        //      let head = head(list);
        //      let tail = tail(list);
        Function<Integer> head = head().returnTo(iff, Name.head);
        Function<ArrayList<Integer>> tail = tail().returnTo(iff, Name.tail);
        iff.propagateOnFalse(Name.list, Name.arg, head);
        iff.propagateOnFalse(Name.list, Name.arg, tail);

        // quicksort(filter(tail, i -> lt(i, head))) + head :: quicksort(filter(tail, i -> gteq(i, head))))
        // +
        Function<ArrayList<Integer>> concat = concat().returnTo(iff, Name.onFalse);
        // ::
        Function<ArrayList<Integer>> cons = cons().returnTo(concat, Name.rightArg);
        iff.propagateOnFalse(Name.head, Name.leftArg, cons);
        // 2 times quicksort(filter(...))
        Function<ArrayList<Integer>> qsortReCallLeft = functionCall(qsortSignature).returnTo(concat, Name.leftArg);
        Function<ArrayList<Integer>> qsortReCallRight = functionCall(qsortSignature).returnTo(cons, Name.rightArg);

        Implementation<ArrayList<Integer>> implLeft = filterSignature();
        Implementation<ArrayList<Integer>> implRight = filterSignature();
        FunctionSignature<ArrayList<Integer>> filterSignatureLeft = implLeft.get();
        FunctionSignature<ArrayList<Integer>> filterSignatureRight = implRight.get();

        // function lt(a, b) = a < b;
        // filter(tail, i -> lt(i, head))
        BinOp<Integer, Integer, Boolean> lt = lt();
        Function<Boolean> ltPredicate = partialApplication(lt, Name.rightArg)
                .propagate(Name.arg, Name.leftArg, lt)
                .propagate(Name.rightArg, Name.rightArg, lt);

        Function<ArrayList<Integer>> filterCallLeft =
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

        Function<ArrayList<Integer>> filterCallRight =
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

        iff.label("iff");
        qsortSignature.label("QSORT");
        filterSignatureLeft.label("**FILTER LEFT");
        filterSignatureRight.label("**FILTER RIGHT");
        qsortReCallLeft.label("qsortReCallLeft");
        qsortReCallRight.label("qsortReCallRight");
        ltPredicate.label("LT(l,r)");
        gtEqPredicate.label("GTEQ(l,r)");
        filterCallLeft.label("filterCallLeft");
        filterCallRight.label("filterCallRight");
        propagateLeftOnCondition.label("condPropLeft");
        propagateRightOnCondition.label("condPropRight");

        return qsortSignature;
    }

    private static ArrayList<Integer> emptyList() {
        return new ArrayList<>();
    }
}
