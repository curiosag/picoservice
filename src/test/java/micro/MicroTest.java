package micro;

import micro.If.If;
import micro.atomicFunctions.*;
import org.junit.Test;

import java.util.function.Supplier;

import static micro.ExTop.TOP;
import static micro.If.PropagationType.*;
import static micro.Names.ping;

public class MicroTest {

    Env env = new Env();

/*
    a   b
    \   /
      + 
      |
    add(a,b)
    
    
    add(a,b)
      |
    main
            

*/


    @Test
    public void testSimpleFunc() {
        F main = f(new Print(), Names.result).setLabel("main");
        F add = f(new AddInt(), Names.left, Names.right).setLabel("add");

        main.addPropagation(Names.a, Names.left, add);
        main.addPropagation(Names.b, Names.right, add);

        Ex ex = main.newExecution(env, TOP);
        ex.accept(Value.of(Names.a, 1, TOP));
        ex.accept(Value.of(Names.b, 2, TOP));
    }

    /*
        function trisum (a,b,c) = a + b + c
        function trimul (a,b,c) = a * b * c
        print(sum(trisum(1,2,3), trimul(1,2,3))

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
            main   (a,b,c)



    * */

    private F createTriOp(Supplier<Atom> getBinOp, String resultName, String label) {
        F op1 = f(getBinOp.get(), Names.left, Names.right).setLabel(label + "_1");
        F op2 = f(getBinOp.get(), Names.left, Names.right).setLabel(label + "_2");
        F triOp = f(null, Names.a, Names.b, Names.c).setLabel(label + "_triop");

        triOp.addPropagation(Names.a, op1);
        triOp.addPropagation(Names.b, op1);
        triOp.addPropagation(Names.c, Names.right, op1);

        op1.addPropagation(Names.a, Names.left, op2);
        op1.addPropagation(Names.b, Names.right, op2);

        op2.returnAs(Names.left);
        op1.returnAs(Names.result);

        triOp.returnAs(resultName);
        return triOp;
    }

    @Test
    public void testSimpleFuncs() {
        F main = f(new Print(), Names.result).setLabel("main");

        F add = f(new AddInt(), Names.left, Names.right).setLabel("add").returnAs(Names.result);
        F triSum = createTriOp(AddInt::new, Names.left, "tsum").returnAs(Names.left);
        F triMul = createTriOp(MulInt::new, Names.right, "tmul").returnAs(Names.right);

        main.addPropagation(Names.a, add);
        main.addPropagation(Names.b, add);
        main.addPropagation(Names.c, add);

        add.addPropagation(Names.a, triSum);
        add.addPropagation(Names.b, triSum);
        add.addPropagation(Names.c, triSum);

        add.addPropagation(Names.a, triMul);
        add.addPropagation(Names.b, triMul);
        add.addPropagation(Names.c, triMul);

        Ex ex = main.newExecution(env, TOP);
        ex.accept(Value.of(Names.a, 1, TOP));
        ex.accept(Value.of(Names.c, 3, TOP));
        ex.accept(Value.of(Names.b, 2, TOP));
    }

    /*
       function dec(a) = a - 1
       print(dec(3));
    */

    @Test
    public void testConst() {
        F main = f(new Print(), Names.result).setLabel("main");
        F dec = f(null, Names.a).setLabel("dec");
        F sub = f(new SubInt(), Names.left, Names.right).setLabel("sub");
        F one = f(new Const(1)).returnAs(Names.right).setLabel("const:one");

        main.addPropagation(ping, dec);
        dec.addPropagation(ping, sub);
        sub.addPropagation(ping, one);

        main.addPropagation(Names.a, dec);
        dec.addPropagation(Names.a, Names.left, sub);

        Ex ex = main.newExecution(env, TOP);
        ex.accept(Value.of(ping, null, TOP));
        ex.accept(Value.of(Names.a, 1, TOP));
    }

    /* function max(left,right) = if (left > right)
                           left
                         else
                           right
       print(gt(3));
    */

    @Test
    public void testIf() {
        F main = f(new Print(), Names.result).setLabel("main");
        F max = f(null, Names.left, Names.right).setLabel("max");
        If iff = new If().setLabel("if");
        F gt = f(new Gt(), Names.left, Names.right).returnAs(Names.condition).setLabel("gt");

        main.addPropagation(Names.left, max);
        main.addPropagation(Names.right, max);

        max.addPropagation(Names.left, iff);
        max.addPropagation(Names.right, iff);

        iff.addPropagation(CONDITION, Names.left, gt);
        iff.addPropagation(CONDITION, Names.right, gt);
        iff.addPropagation(ON_TRUE, Names.left, Names.result, iff);
        iff.addPropagation(ON_FALSE, Names.right, Names.result, iff);

        Ex ex = main.newExecution(env, TOP);
        ex.accept(Value.of(Names.left, 1, TOP));
        ex.accept(Value.of(Names.right, 2, TOP));

        ex = main.newExecution(env, TOP);
        ex.accept(Value.of(Names.left, 2, TOP));
        ex.accept(Value.of(Names.right, 1, TOP));
    }

    /* function geo(a) = if (a = 0)
                           0
                         else
                           a + sum(a - 1);
       print(sum(3));
    */

    @Test
    public void testSimpleRecursion() {
        F main = f(new Print(), Names.result).setLabel("main");
        F geo = f(null, Names.a);
        If iff = new If().setLabel("if");
        F eq = f(new Eq(), Names.left, Names.right).setLabel("eq");
        F add = f(new AddInt(), Names.left, Names.right).setLabel("add");
        F sub = f(new SubInt(), Names.left, Names.right).setLabel("sub");
        F c_zero_eq = cönst(0).setLabel("zero:eq").returnAs(Names.right);
        F c_zero_onTrue = cönst(0).setLabel("zero:ontrue").returnAs(Names.result);
        F c_one = cönst(1).setLabel("one").returnAs(Names.right);

        main.addPropagation(ping, geo);
        geo.addPropagation(ping, iff);
        iff.addPropagation(TRANSIT, ping, eq);
        iff.addPropagation(ON_TRUE, ping, c_zero_eq);

        eq.addPropagation(ping, c_zero_eq);

        main.addPropagation(Names.a, geo);
        geo.addPropagation(Names.a, iff);

    }

    private F cönst(Object i) {
        return f(new Const(i), ping);
    }

    private F f(Atom atom, String... params) {
        return new F(atom, params);
    }

}
