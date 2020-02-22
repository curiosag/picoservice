package micro;

import micro.If.If;
import micro.atoms.AddInt;
import micro.atoms.Atom;
import micro.atoms.Const;
import micro.atoms.MulInt;
import org.junit.Test;

import java.util.function.Supplier;

import static micro.F.f;
import static micro.If.If.iff;
import static micro.If.PropagationType.*;
import static micro.Names.ping;
import static micro.atoms.AddInt.addInt;
import static micro.atoms.Eq.eq;
import static micro.atoms.Gt.gt;
import static micro.atoms.Nop.nop;
import static micro.atoms.Print.print;
import static micro.atoms.SubInt.subInt;
import static micro.partialapp.FPartialApp.partial;

public class MicroTest {

    Env env = new Env();
    ExTop TOP = new ExTop(env);

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
        F main = f(print(), Names.result).label("main");
        F add = f(addInt(), Names.left, Names.right).label("add");

        main.addPropagation(Names.a, Names.left, add);
        main.addPropagation(Names.b, Names.right, add);

        Ex ex = main.createExecution(env, TOP);
        ex.process(Value.of(Names.a, 1, TOP));
        ex.process(Value.of(Names.b, 2, TOP));
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
        F op1 = f(getBinOp.get(), Names.left, Names.right).label(label + "_1");
        F op2 = f(getBinOp.get(), Names.left, Names.right).label(label + "_2");
        F triOp = f(nop, Names.a, Names.b, Names.c).label(label + "_triop");

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
        F main = f(print(), Names.result).label("main");

        F add = f(addInt(), Names.left, Names.right).label("add").returnAs(Names.result);
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

        Ex ex = main.createExecution(env, TOP);
        ex.process(Value.of(Names.a, 1, TOP));
        ex.process(Value.of(Names.c, 3, TOP));
        ex.process(Value.of(Names.b, 2, TOP));
    }

    /*
       function dec(a) = a - 1
       print(dec(1));
    */

    @Test
    public void testConst() {
        F main = f(print(), Names.result).label("main");
        F dec = f(nop, Names.a).label("dec");
        F sub = f(subInt(), Names.left, Names.right).label("sub");
        F one = f(new Const(1)).returnAs(Names.right).label("const:one");

        main.addPropagation(ping, dec);
        dec.addPropagation(ping, sub);
        sub.addPropagation(ping, one);

        main.addPropagation(Names.a, dec);
        dec.addPropagation(Names.a, Names.left, sub);

        Ex ex = main.createExecution(env, TOP);
        ex.process(Value.of(ping, null, TOP));
        ex.process(Value.of(Names.a, 1, TOP));
    }

    /*
       function dec(a) =
         let delta = 1
         a - delta

         print(dec(1));
    */

    @Test
    public void testLetPartial() {
        _F main = f(print(), Names.result).label("main");
        _F dec = f(nop, Names.a).label("dec");
        _F sub = partial(f(subInt(), Names.left, Names.right).label("sub"))
                .withParam(Names.left).resolvedBy(Names.delta);

        main.addPropagation(Names.a, dec);
        dec.addPropagation(Names.a, Names.left, sub);

        Ex ex = main.createExecution(env, TOP);
        ex.process(Value.of(Names.a, 1, TOP));
    }

    /* function max(left,right) = if (left > right)
                           left
                         else
                           right
       print(gt(3));
    */

    @Test
    public void testIf() {
        F main = f(print(), Names.result).label("main");
        F max = f(nop, Names.left, Names.right).label("max");
        If iff = iff().label("if");
        F gt = f(gt(), Names.left, Names.right).returnAs(Names.condition).label("gt");

        main.addPropagation(Names.left, max);
        main.addPropagation(Names.right, max);

        max.addPropagation(Names.left, iff);
        max.addPropagation(Names.right, iff);

        iff.addPropagation(CONDITION, Names.left, gt);
        iff.addPropagation(CONDITION, Names.right, gt);
        iff.addPropagation(ON_TRUE, Names.left, Names.result, iff);
        iff.addPropagation(ON_FALSE, Names.right, Names.result, iff);

        Ex ex = main.createExecution(env, TOP);
        ex.process(Value.of(Names.left, 1, TOP));
        ex.process(Value.of(Names.right, 2, TOP));

        ex = main.createExecution(env, TOP);
        ex.process(Value.of(Names.left, 2, TOP));
        ex.process(Value.of(Names.right, 1, TOP));
    }

    /* function geo(a) =

                         if (a = 0)
                           0
                         else
                         {
                           let next_a = a - 1
                           a + geo(next_a);
                         }
       print(geo(3));
    */

    @Test
    public void testSimpleRecursion() {
        F main = f(print(), Names.result).label("main");
        F geo = f(nop, Names.a).label("geo");
        If iff = iff().label("if");

        main.addPropagation(Names.a, geo);
        geo.addPropagation(Names.a, iff);

        // condition
        F eq = f(eq(), Names.left, Names.right).returnAs(Names.condition).label("eq");

        iff.addPropagation(CONDITION, Names.a, Names.left, eq);
        eq.addPropagation(Names.left, ping, CONST(0).returnAs(Names.right).label("zero:eq"));
        // onTrue
        iff.addPropagation(ON_TRUE, Names.a, ping, CONST(0).label("zero:ontrue"));
        // onFalse
        F block_else = f(nop).label("block_else");
        iff.addPropagation(ON_FALSE, Names.a, block_else);
        // let next_a = a - 1
        String next_a = "next_a";
        F sub = f(subInt(), Names.left, Names.right).returnAs(next_a).label("sub");
        block_else.addPropagation(Names.a, Names.left, sub);
        sub.addPropagation(Names.left, ping, CONST(1).returnAs(Names.right).label("one"));
        // a + geo(next_a);
        F add = f(addInt(), Names.left, Names.right).label("add");
        F geoReCall = new FCall(geo).returnAs(Names.right).label("geoCallR");

        block_else.addPropagation(Names.a, Names.left, add);
        block_else.addPropagation(next_a, add);
        add.addPropagation(next_a, Names.a, geoReCall);

        main.createExecution(env, TOP).process(Value.of(Names.a, 0, TOP));

    }

    private F CONST(Object i) {
        return f(new Const(i), ping);
    }

}
