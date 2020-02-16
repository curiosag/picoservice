package micro;

import micro.nativeFunctions.AddInt;
import micro.nativeFunctions.MulInt;
import micro.nativeFunctions.Print;
import org.junit.Test;

import java.util.function.Supplier;

import static micro.ExTop.TOP;

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

    private F f(FAtom atom, String... params) {
        return new F(atom, params);
    }

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

    private F createTriOp(Supplier<FAtom> getBinOp, String resultName, String label) {
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
        F triSum = createTriOp(AddInt::new, Names.left,"tsum").returnAs(Names.left);
        F triMul = createTriOp(MulInt::new, Names.right,"tmul").returnAs(Names.right);

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


}
