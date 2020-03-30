package micro;

import micro.If.If;
import micro.atoms.*;
import nano.ingredients.Name;
import org.junit.Test;

import java.util.function.Supplier;

import static micro.If.If.iff;
import static micro.Names.paramFVar;
import static micro.Names.ping;
import static micro.PropagationType.*;
import static micro.atoms.Add.add;
import static micro.atoms.Eq.eq;
import static micro.atoms.Gt.gt;
import static micro.atoms.Mul.mul;
import static micro.atoms.Primitive.nop;
import static micro.atoms.Print.print;
import static micro.atoms.Sub.subInt;
import static org.junit.Assert.assertEquals;

public class MicroTest {

    private final Address address = new Address(new byte[0], 1, 1);
    private Node node = new Node(address);


    @Test
    public void testLet() {

        /*

        func f(){
            let a = 1;
            let b = a * a;
            return a + b;
          }

        main{
         f()
        }

        */

        ResultCollector result = new ResultCollector();
        Action resultListener = new Action(i -> result.set(i.values()));

        F f = f(nop, Names.a).returnAs(Names.output).label("f");
        F mul = f(mul(), Names.left, Names.right).label("mul").returnAs(Names.b);
        F add = f(add(), Names.left, Names.right).label("add");

        f.addPropagation(ping, CONST(2).returnAs(Names.a).label("const:2"));
        f.addPropagation(Names.a, Names.left, mul);
        f.addPropagation(Names.a, Names.right, mul);

        f.addPropagation(Names.a, Names.left, add);
        f.addPropagation(Names.b, Names.right, add);

        F main = f(resultListener, Names.output).label("main");
        main.addPropagation(ping, f);

        _Ex ex = main.createExecution(node, node.getTop());

        node.start();

        ex.receive(Value.of(ping, 0, node.getTop()));

        Concurrent.await(() -> !result.isEmpty());
        assertEquals(6, result.get().get(0).get());
    }


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
        F add = f(add(), Names.left, Names.right).label("add");

        main.addPropagation(Names.a, Names.left, add);
        main.addPropagation(Names.b, Names.right, add);

        _Ex ex = main.createExecution(node, node.getTop());
        ex.receive(Value.of(Names.a, 1, node.getTop()));
        ex.receive(Value.of(Names.b, 2, node.getTop()));
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

    private F createTriOp(Supplier<Primitive> getBinOp, String resultName, String label) {
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

        F add = f(add(), Names.left, Names.right).label("add").returnAs(Names.result);
        F triSum = createTriOp(Add::new, Names.left, "tsum").returnAs(Names.left);
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

        _Ex TOP = node.getTop();
        _Ex ex = main.createExecution(node, TOP);
        ex.receive(Value.of(Names.a, 1, TOP));
        ex.receive(Value.of(Names.c, 3, TOP));
        ex.receive(Value.of(Names.b, 2, TOP));
    }



    /*

        def sub(a: Int, b: Int): Int = a - b

        def useFVar(a: Int, b: Int):Int = {
          val subtract  = sub
          subtract(a, b)
        }

       print(useFVar(2,1));

    */

    @Test
    public void testFunctionalValue() {
        ResultCollector result = new ResultCollector();
        Action resultListener = new Action(i -> result.set(i.values()));

        F main = f(resultListener, Names.output).label("main");

        F sub = f(subInt(), Names.left, Names.right).label("sub");
        F useFVar = f(nop, Names.a, Names.b).label("useFVar").returnAs(Names.output);

        // a plain functional value is a partially applied function with no partial parameters
        F createSubtract = new FCreatePartiallyAppliedFunction(node, sub).returnAs(paramFVar).label("createSubtract");
        F callSubtract = new FCallByFunctionalValue(node, paramFVar, Names.left, Names.right).label("callSubtract");

        useFVar.addPropagation(Names.a, ping, createSubtract);
        useFVar.addPropagation(paramFVar, callSubtract);
        useFVar.addPropagation(Name.a, Names.left, callSubtract);
        useFVar.addPropagation(Name.b, Names.right, callSubtract);

        main.addPropagation(Names.a, useFVar);
        main.addPropagation(Names.b, useFVar);

        _Ex ex = main.createExecution(node, node.getTop());

        node.start();

        ex.receive(Value.of(Names.a, 2, node.getTop()));
        ex.receive(Value.of(Names.b, 1, node.getTop()));

        Concurrent.await(() -> !result.isEmpty());
        assertEquals(1, result.get().get(0).get());
    }


    /*

        def sub(a: Int, b: Int): Int = a - b

        def usePartial(a: Int, b: Int):Int = {
          val dec : Int => Int = sub(_, b)
          dec(a)
        }

       print(usePartial(2,3));

    */

    @Test
    public void testPartialFunctionApplication() {

        ResultCollector result = new ResultCollector();
        Action resultListener = new Action(i -> result.set(i.values()));

        F main = f(resultListener, Names.output).label("main");

        F sub = f(subInt(), Names.left, Names.right).label("sub");
        F usePartial = f(nop, Names.a, Names.b).returnAs(Names.output).label("usePartial");

        F createDec = new FCreatePartiallyAppliedFunction(node, sub, Names.right).returnAs(paramFVar).label("createDec");
        F callDec = new FCallByFunctionalValue(node, paramFVar, Names.left).label("callDec");

        usePartial.addPropagation(Names.b, Names.right, createDec);
        usePartial.addPropagation(paramFVar, callDec);
        usePartial.addPropagation(Name.a, Names.left, callDec);

        main.addPropagation(Names.a, usePartial);
        main.addPropagation(Names.b, usePartial);

        _Ex ex = main.createExecution(node, node.getTop());

        node.start();

        ex.receive(Value.of(Names.a, 2, node.getTop()));
        ex.receive(Value.of(Names.b, 1, node.getTop()));

        Concurrent.await(() -> !result.isEmpty());
        assertEquals(1, result.get().get(0).get());
    }

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
        _Ex TOP = node.getTop();
        _Ex ex = main.createExecution(node, TOP);
        ex.receive(Value.of(ping, null, TOP));
        ex.receive(Value.of(Names.a, 1, TOP));
    }

    @Test
    public void testIf() {
        F main = f(print(), Names.result).label("main");
        F max = f(nop, Names.left, Names.right).label("max");
        If iff = iff(node).label("if");
        F gt = f(gt(), Names.left, Names.right).returnAs(Names.condition).label("gt");

        main.addPropagation(Names.left, max);
        main.addPropagation(Names.right, max);

        max.addPropagation(Names.left, iff);
        max.addPropagation(Names.right, iff);

        iff.addPropagation(CONDITION, Names.left, gt);
        iff.addPropagation(CONDITION, Names.right, gt);
        iff.addPropagation(TRUE_BRANCH, Names.left, Names.result, iff);
        iff.addPropagation(FALSE_BRANCH, Names.right, Names.result, iff);
        _Ex TOP = node.getTop();
        _Ex ex = main.createExecution(node, TOP);
        ex.receive(Value.of(Names.left, 1, TOP));
        ex.receive(Value.of(Names.right, 2, TOP));

        ex = main.createExecution(node, TOP);
        ex.receive(Value.of(Names.left, 2, TOP));
        ex.receive(Value.of(Names.right, 1, TOP));
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
        F main = createRecSum();

        node.setDelay(0);
        node.start();

        _Ex m1 = node.getExecution(main);
        //  _Ex m2 = env.getExecution(main);

        m1.receive(Value.of(Names.a, 100, m1.returnTo()));
        //m2.receive(Value.of(Names.a, 100, m1.returnTo()));

        //Concurrent.sleep(1000);
        //node.log("stopping");
        //node.stop();
        Concurrent.sleep(5000);
        node.close();
    }


    @Test
    public void testResumeSimpleRecursion() {
        F main = createRecSum();
        node.setDelay(1);
        node.recover();
        node.start();
        Concurrent.sleep(50000);
        node.close();
    }

    private F createRecSum() {
        F main = f(print(), Names.result).label("main");
        F geo = f(nop, Names.a).label("geo");
        If iff = iff(node).label("if");

        main.addPropagation(Names.a, geo);
        geo.addPropagation(Names.a, iff);

        // condition
        F eq = f(eq(), Names.left, Names.right).returnAs(Names.condition).label("eq");

        iff.addPropagation(CONDITION, Names.a, Names.left, eq);
        eq.addPropagation(Names.left, ping, CONST(0).returnAs(Names.right).label("zero:eq"));
        // onTrue
        iff.addPropagation(TRUE_BRANCH, Names.a, ping, CONST(0).label("zero:ontrue"));
        // onFalse
        F block_else = f(nop).label("block_else");
        iff.addPropagation(FALSE_BRANCH, Names.a, block_else);
        // let next_a = a - 1
        String next_a = "next_a";
        F sub = f(subInt(), Names.left, Names.right).returnAs(next_a).label("sub");
        block_else.addPropagation(Names.a, Names.left, sub);
        sub.addPropagation(Names.left, ping, CONST(1).returnAs(Names.right).label("one"));
        // a + geo(next_a);
        F add = f(add(), Names.left, Names.right).label("add");
        F geoReCall = new FCall(node, geo).returnAs(Names.right).label("geoCallR");

        block_else.addPropagation(Names.a, Names.left, add);
        block_else.addPropagation(next_a, add);
        add.addPropagation(next_a, Names.a, geoReCall);
        return main;
    }

    private F CONST(Object i) {
        return F.f(node, new Const(i), ping);
    }

    private F f(Primitive primitive, String... params) {
        return F.f(node, primitive, params);
    }


}
