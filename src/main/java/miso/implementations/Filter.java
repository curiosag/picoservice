package miso.implementations;

import miso.ingredients.*;
import miso.ingredients.nativeImpl.ListBinOps;

import java.util.ArrayList;
import java.util.List;

import static miso.ingredients.FunctionCall.functionCall;
import static miso.ingredients.FunctionSignature.functionSignature;
import static miso.ingredients.Iff.iffList;
import static miso.ingredients.nativeImpl.ListBinOps.cons;
import static miso.ingredients.nativeImpl.UnOps.head;
import static miso.ingredients.nativeImpl.UnOps.tail;

public class Filter {

    public static FunctionSignature<List<Integer>> getFilterSignature() {

        /*
        predicate: arg -> boolean

        function filter(list, predicate) = {
            iff (list == []){                    //outerIff
                []
            } else {
                let head = head(list);
                let tail = tail(list);
                iff (predicate(head))           //innerIff
                    head :: filter(tail, predicate)
                else
                    filter(tail, predicate)
            }
        }
        */

        List<Integer> empty = new ArrayList<>();

        Iff<List<Integer>> outerIff = iffList();
        outerIff.constant(Name.onTrue, empty);
        FunctionSignature<List<Integer>> filterSignature = functionSignature(outerIff);
        filterSignature.propagate(Name.predicate, Name.predicate, outerIff);
        filterSignature.propagate(Name.list, Name.list, outerIff);
        filterSignature.label("FILTER");

        Function<Boolean> listEq = ListBinOps.eq().returnTo(outerIff, Name.condition).constant(Name.rightArg, empty);
        outerIff.propagate(Name.list, Name.leftArg, listEq);

        Iff<List<Integer>> innerIff = iffList();
        innerIff.label("innerIff");
        outerIff.label("outerIff");
        innerIff.returnTo(outerIff, Name.onFalse);
        outerIff.propagateOnFalse(Name.predicate, Name.predicate, innerIff);

        //let head = head(list);
        //let tail = tail(list);
        Function<Integer> head = head().returnTo(outerIff, Name.head);
        Function<List<Integer>> tail = tail().returnTo(outerIff, Name.tail);

        outerIff.propagateOnFalse(Name.list, Name.arg, head);
        outerIff.propagateOnFalse(Name.list, Name.arg, tail);
        outerIff.propagateOnFalse(Name.head, Name.head, innerIff);
        outerIff.propagateOnFalse(Name.tail, Name.tail, innerIff);

        /*
        else {
                ...

                iff (predicate(head))           //innerIff
                    head :: filter(tail, predicate)
                else
                    filter(tail, predicate)
            }
        */

        FunctionStub<Boolean> predicateStub = FunctionStub.of(Name.predicate);
        predicateStub.returnTo(innerIff, Name.condition);

        Function<List<Integer>> cons = cons().returnTo(innerIff, Name.onTrue);
        Function<List<Integer>> filterReCallOnTrue = functionCall(filterSignature).returnTo(cons, Name.rightArg);
        Function<List<Integer>> filterReCallOnFalse = functionCall(filterSignature).returnTo(innerIff, Name.onFalse);
        filterReCallOnTrue.label("filterReCallOnTrue");
        filterReCallOnFalse.label("filterReCallOnFalse");

        innerIff.propagate(Name.head, Name.arg, predicateStub);
        innerIff.propagate(Name.predicate, Name.predicate, predicateStub);

        innerIff.propagateOnTrue(Name.head, Name.leftArg, cons);
        innerIff.propagateOnTrue(Name.tail, Name.list, filterReCallOnTrue);
        innerIff.propagateOnTrue(Name.predicate, Name.predicate, filterReCallOnTrue);

        innerIff.propagateOnFalse(Name.tail, Name.list, filterReCallOnFalse);
        innerIff.propagateOnFalse(Name.predicate, Name.predicate, filterReCallOnFalse);

        return filterSignature;
    }

}
