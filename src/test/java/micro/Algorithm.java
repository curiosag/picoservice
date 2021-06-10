package micro;

import micro.If.If;
import micro.primitives.*;
import micro.primitives.Lists.*;

import java.util.Collections;

import static micro.If.If.iff;
import static micro.Names.*;
import static micro.PropagationType.*;
import static micro.primitives.Add.add;
import static micro.primitives.Mul.mul;
import static micro.primitives.Primitive.nop;

public class Algorithm {

    /*
        given lteq: (u,v) -> boolean

        function quicksort(list) = {
            iff (list == []){
                []
            } else {
                let head = head(list);
                let tail = tail(list);
                let testGt = lteq(_, head) // partially applied function
                let testLteq = lteq(_, head)  // partially applied function
                let filteredGt = filter(tail, testGt)
                let filteredLteq = filter(tail, testLteq)
                let sortedGt = quicksort(filteredGt)
                let sortedLteq = quicksort(filteredLteq)
                sortedLteq ::  head :: sortedGt
            }
        }
        */

    public static F createQuicksort(Env env) {
        String predicateTestGt = "predicateTestGt";
        String predicateTestLteq = "predicateTestLteq";
        String filteredGt = "filteredGt";
        String filteredLteq = "filteredLteq";
        String sortedGt = "sortedGt";
        String sortedLteq = "sortedLteq";
        String consed = "consed";

        F quicksort = new F(env, nop, list).label("quicksort");
        If if_listEmpty = iff(env).label("if:listEmpty");
        quicksort.addPropagation(list, if_listEmpty);

        F isEmpty = new F(env, IsEmpty.isEmpty, list).returnAs(condition).label("isEmpty");
        if_listEmpty.addPropagation(COND_CONDITION, list, isEmpty);
        F constEmptyList = new F(env, new Constant(Collections.emptyList()), ping).label("const:emptylist()");
        if_listEmpty.addPropagation(COND_TRUE_BRANCH, list, ping, constEmptyList);

        F block_else = f(env, nop).label("block_else");
        if_listEmpty.addPropagation(COND_FALSE_BRANCH, list, block_else);

        F head = new F(env, Head.head, list).returnAs(Names.head).label("head()");
        F tail = new F(env, Tail.tail, list).returnAs(Names.tail).label("tail()");
        block_else.addPropagation(list, head);
        block_else.addPropagation(list, tail);

        F gt = f(env, Gt.gt, Names.left, Names.right).label("gt?");
        F createTestGt = new FCreatePartiallyAppliedFunction(env, gt, Names.right).returnAs(predicateTestGt).label("createTestGt");
        block_else.addPropagation(Names.head, right, createTestGt);

        F lteq = f(env, Lteq.lteq, Names.left, Names.right).label("lteq?");
        F createTestLteq = new FCreatePartiallyAppliedFunction(env, lteq, Names.right).returnAs(predicateTestLteq).label("createTestLteq");
        block_else.addPropagation(Names.head, right, createTestLteq);

        F filter = createFilter(env);
        F filterCallGt = new FCall(env, filter).returnAs(filteredGt).label("filterCallGt");
        F filterCallLteq = new FCall(env, filter).returnAs(filteredLteq).label("filterCallLteq");

        block_else.addPropagation(predicateTestGt, predicate, filterCallGt);
        block_else.addPropagation(predicateTestLteq, predicate, filterCallLteq);
        block_else.addPropagation(Names.tail, list, filterCallGt);
        block_else.addPropagation(Names.tail, list, filterCallLteq);

        F qsortRecallGt = new FCall(env, quicksort).returnAs(sortedGt).label("qsortRecallGt");
        F qsortRecallLteq = new FCall(env, quicksort).returnAs(sortedLteq).label("qsortRecallLteq");
        block_else.addPropagation(filteredGt, Names.list, qsortRecallGt);
        block_else.addPropagation(filteredLteq, Names.list, qsortRecallLteq);

        F cons = new F(env, Cons.cons, element, list).returnAs(consed).label("cons");
        block_else.addPropagation(Names.head, Names.element, cons);
        block_else.addPropagation(sortedGt, list, cons);

        F concat = new F(env, Concat.concat, left, right).label("concat");
        block_else.addPropagation(consed, right, concat);
        block_else.addPropagation(sortedLteq, Names.left, concat);


        return quicksort;
    }

    /*
        given predicate: arg -> boolean

        function filter(list, predicate) = {
            iff (list == []){
                []
            } else {
                let head = head(list);
                let tail = tail(list);
                let tail_filtered = filter(tail, predicate)
                iff (predicate(head))
                    head :: tail_filtered
                else
                    tail_filtered
            }
        }
        */

    public static F createFilter(Env env) {
        String tailFiltered = "tailFiltered";

        F filter = new F(env, nop, predicate, list).label("filter");
        If if_listEmpty = iff(env).label("if:listEmpty");
        filter.addPropagation(list, if_listEmpty);
        filter.addPropagation(predicate, if_listEmpty);

        F isEmpty = new F(env, IsEmpty.isEmpty, list).returnAs(condition).label("isEmpty");
        if_listEmpty.addPropagation(COND_CONDITION, list, isEmpty);
        F constEmptyList = new F(env, new Constant(Collections.emptyList()), ping).label("const:emptylist");
        if_listEmpty.addPropagation(COND_TRUE_BRANCH, list, ping, constEmptyList);

        F block_else = f(env, nop).label("block_else");
        if_listEmpty.addPropagation(COND_FALSE_BRANCH, list, block_else);
        if_listEmpty.addPropagation(COND_FALSE_BRANCH, predicate, block_else);

        F head = new F(env, Head.head, list).returnAs(Names.head).label("head");
        F tail = new F(env, Tail.tail, list).returnAs(Names.tail).label("tail");
        F filterReCall = new FCall(env, filter).returnAs(tailFiltered).label("filterReCall");

        If if_predicate = iff(env).label("if:predicate");
        block_else.addPropagation(list, head);
        block_else.addPropagation(list, tail);
        block_else.addPropagation(Names.tail, Names.list, filterReCall);
        block_else.addPropagation(predicate, filterReCall);

        block_else.addPropagation(predicate, if_predicate);
        block_else.addPropagation(Names.head, if_predicate);
        block_else.addPropagation(tailFiltered, if_predicate);

        // underlying partial function takes left/right, right is already applied, left needs to be supplied
        // TODO: would be nice to have a remapping somewhere for the remaining 1 parameter, "left" -> "argument" or so
        F callPredicate = new FunctionalValueDefinition(env, predicate, Names.left).returnAs(condition).label("callPredicateByFVal");

        if_predicate.addPropagation(COND_CONDITION, predicate, callPredicate);
        if_predicate.addPropagation(COND_CONDITION, Names.head, Names.left, callPredicate);

        F cons = new F(env, Cons.cons, element, list).label("cons");
        if_predicate.addPropagation(COND_TRUE_BRANCH, Names.head, element, cons);
        if_predicate.addPropagation(COND_TRUE_BRANCH, tailFiltered, Names.list, cons);

        if_predicate.addPropagation(COND_FALSE_BRANCH, tailFiltered, new F(env, new Val(), tailFiltered).label("VAL:tailFiltered"));

        return filter;
    }

     /* simple geometrical series geo(n) = 1 + 2 + ... + n-1 + n

       function geo(a) = if (a = 0)
                           0
                         else
                         {
                           let next_a = a - 1
                           a + geo(next_a);
                         }

        Its bit of "legacy code" where some propagations don't follow the function call structure. The tail
        recursive version below is more canonical.

    */

    public static F createRecSum(Env env) {
        F geo = f(env, nop, Names.a).label("geo");
        If iff = iff(env).label("if");

        geo.addPropagation(Names.a, iff);

        // condition
        F eq = f(env, Eq.eq, Names.left, Names.right).returnAs(Names.condition).label("eq");

        iff.addPropagation(COND_CONDITION, Names.a, Names.left, eq);
        eq.addPropagation(Names.left, ping, CONST(env, 0).returnAs(Names.right).label("zero"));
        // onTrue
        iff.addPropagation(COND_TRUE_BRANCH, Names.a, ping, CONST(env, 0).label("zero"));
        // onFalse
        F block_else = f(env, nop).label("block_else");
        iff.addPropagation(COND_FALSE_BRANCH, Names.a, block_else);
        // let next_a = a - 1
        String next_a = "next_a";
        F sub = f(env, Sub.sub, Names.left, Names.right).returnAs(next_a).label("sub");
        block_else.addPropagation(Names.a, Names.left, sub);
        sub.addPropagation(Names.left, ping, CONST(env, 1).returnAs(Names.right).label("one"));
        // a + geo(next_a);
        F add = f(env, add(), Names.left, Names.right).label("add");
        F geoReCall = new FCall(env, geo).returnAs(Names.right).label("geoCallR");

        block_else.addPropagation(Names.a, Names.left, add);
        block_else.addPropagation(next_a, add);
        add.addPropagation(next_a, Names.a, geoReCall);
        return geo;
    }

      /* the same with tail recursion

             function geo(a, cumulated) =
                         if (a = 0)
                           cumulated
                         else
                         {
                           let next_a = a - 1
                           let c = cumulated + next_a
                           geo(next_a, c);
                         }

    */

    public static F createTailRecSum(Env env) {
        F geo = f(env, nop, Names.a, Names.c).tailRecursive().label("geo tailrec");
        If iff = iff(env).label("if");

        // one can't use the same names in the body wihout causing iff to produce a fake return value
        geo.addPropagation(Names.a, Names._a, iff);
        geo.addPropagation(Names.c, Names._c, iff);

        // condition
        F eq = f(env, Eq.eq, Names.left, Names.right).returnAs(Names.condition).label("eq");

        iff.addPropagation(COND_CONDITION, Names._a, Names.left, eq);
        eq.addPropagation(Names.left, ping, CONST(env, 0).returnAs(Names.right).label("zero"));
        // onTrue
        iff.addPropagation(COND_TRUE_BRANCH, Names._c, result, iff);
        // onFalse
        // let next_a = a - 1
        String next_a = "next_a";
        String cumulated = "cumulated";

        F sub = f(env, Sub.sub, Names.left, Names.right).returnAs(next_a).label("sub");
        sub.addPropagation(Names.left, ping, CONST(env, 1).returnAs(Names.right).label("one"));

        iff.addPropagation(COND_FALSE_BRANCH, Names._a, Names.left, sub);
        // let cum = cumulated + next_a
        F add = f(env, add(), Names.left, Names.right).returnAs(cumulated).label("add");
        iff.addPropagation(COND_FALSE_BRANCH, next_a, Names.left, add);
        iff.addPropagation(COND_FALSE_BRANCH, Names._c, Names.right, add);

        iff.addPropagation(PropagationType.COND_INDISCRIMINATE, cumulated, Names.c, geo);
        iff.addPropagation(PropagationType.COND_INDISCRIMINATE, next_a, Names.a, geo);
        iff.doneOn(next_a, cumulated); // need to define when it is done, since it doesn't follow the usual result mechanism

        return geo;
    }

       /*
        function add(left,right)=left+right
        function mul(left,right)=left*right
        function trisum (a,b,c) = add(add(a, b), c)
        function trimul (a,b,c) = mul(mul(a, b), c)
        function calc(a,b,c) = sum(trisum(1,2,3), trimul(1,2,3))

        a   b
         \ /
   (add2) +   c
           \ /
            + (add1)
            |
            trisum (a,b,c)


        a   b
         \ /
   (mul1) *   c
           \ /
            * (mul2)
            |
            trimul   (a,b,c)


trisum(a,b,c)   trimul(a,b,c)
           \   /
             + (add)
             |
            calc   (a,b,c)



    * */

    private static F createTriOp(Env env, F binOp, String label) {
        F op1 = new FCall(env, binOp).label(label + "_1");
        F op2 = new FCall(env, binOp).label(label + "_2");

        F triOp = f(env, nop, Names.a, Names.b, Names.c).label(label + "_triop");
        triOp.addPropagation(Names.a, op1);
        triOp.addPropagation(Names.b, op1);
        triOp.addPropagation(Names.c, Names.right, op1);

        op1.addPropagation(Names.a, Names.left, op2);
        op1.addPropagation(Names.b, Names.right, op2);

        op2.returnAs(Names.left);
        op1.returnAs(Names.result);

        return triOp;
    }

    public static F createCalc(Env env) {
        F calc = f(env, nop, Names.result).label("calc");

        // single instances of add and mul, accessed via FCalls
        F add = f(env, add(), Names.left, Names.right).label("add").returnAs(Names.result);
        F mul = f(env, mul(), Names.left, Names.right).label("mul").returnAs(Names.result);
        F triSum = createTriOp(env, add, "triSum").returnAs(Names.left);
        F triMul = createTriOp(env, mul, "triMul").returnAs(Names.right);

        F callAdd = new FCall(env, add).returnAs(Names.result);
        calc.addPropagation(Names.a, callAdd);
        calc.addPropagation(Names.b, callAdd);
        calc.addPropagation(Names.c, callAdd);

        //TODO: shouldn't all propagations pass through the called function without being bypassed to triSum/triMul?
        callAdd.addPropagation(Names.a, triSum);
        callAdd.addPropagation(Names.b, triSum);
        callAdd.addPropagation(Names.c, triSum);

        callAdd.addPropagation(Names.a, triMul);
        callAdd.addPropagation(Names.b, triMul);
        callAdd.addPropagation(Names.c, triMul);
        return calc;
    }

    /*
     * max(left, right) = if(left > right) left else right
     *
     * */
    public static F createMax(Env env) {
        F max = f(env, nop, Names.left, Names.right).label("max");
        If iff = iff(env).label("if");
        F gt = f(env, Gt.gt, Names.left, Names.right).returnAs(Names.condition).label("gt");

        max.addPropagation(Names.left, iff);
        max.addPropagation(Names.right, iff);

        //TODO possible that nothing is stashed? Also in useFVar?
        iff.addPropagation(COND_CONDITION, Names.left, gt);
        iff.addPropagation(COND_CONDITION, Names.right, gt);
        iff.addPropagation(COND_TRUE_BRANCH, Names.left, Names.result, iff);
        iff.addPropagation(COND_FALSE_BRANCH, Names.right, Names.result, iff);

        return max;
    }

    public static F f(Env env, Primitive primitive, String... params) {
        return F.f(env, primitive, params);
    }

    public static F CONST(Env env, Object i) {
        return F.f(env, new Constant(i), ping);
    }

}
