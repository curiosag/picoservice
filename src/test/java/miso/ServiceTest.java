package miso;

import miso.ingredients.*;
import miso.message.Adresses;
import miso.message.Message;
import miso.message.Name;
import org.junit.Ignore;
import org.junit.Test;

import static miso.ingredients.Action.action;
import static miso.ingredients.Add.add;
import static miso.ingredients.Eq.eq;
import static miso.ingredients.Function.function;
import static miso.ingredients.Gt.gt;
import static miso.ingredients.If.condInt;
import static miso.ingredients.Mul.mul;
import static miso.ingredients.Sub.sub;

public class ServiceTest {

    private static Long zero = 0L;

    private static final Integer _8 = 8;
    private static final Integer _5 = 5;
    private static final Integer _4 = 4;
    private static final Integer _2 = 2;
    private static final Integer _1 = 1;
    private static final Integer _0 = 0;

    private static Action printMsg = action(i -> System.out.println(i.toString()));
    static{
        new Thread(printMsg).start();
    }

    private static Address main = new Address("main");

    private Message message(String key, Object value) {
        return Message.of(key, value, main, OpId.opId(0L, 0));
    }


    //@Test
    public void testFunc() {

        /*

            function add(v1, v2) = (v1 + v2);

            function max(v1, v2) = if (v1 > v2)
                                        v1
                                   else
                                        v2

            function fac(v) = if (v = 0 )       // v, 0, 1, 1 -> v, i0, i11, i12
                                        1
                                   else
                                        v * fac(v - 1);


            const a = add(0, 0);                // addA
            const b = add(add(0, 0),add(1, 1)); // addB (addB1, addB2)
            const c = add(a, b);                // addC

          r = if (i1 == i2)                         //  cond(cond_eq)
                       max(i1 + a + c, a + i2)      //      cond_max(addM1(i1, addM11(a, c)), addM2(a, i2))
                   else                             //
                      fac(i1 + i2)                  //      fac...


         */

        If<Integer> facCond = start(condInt(start(eq())));

        Func<Integer> sub = start(sub());
        Func<Integer> mul = start(mul());

        facCond.cond.recieve(message(Name.leftArg, _0));
        facCond.cond.recieve(message(Name.rightArg, _0));

        facCond.recieve(message(Name.onTrue, 1));


        Func<Integer> fac = null;


        Func<Integer> a = add();
        Func<Integer> b = add();
        Func<Integer> b1 = add();
        Func<Integer> b2 = add();
        Func<Integer> c = add();

        b1.addTarget(Name.leftArg, b);
        b2.addTarget(Name.rightArg, b);
        a.addTarget(Name.leftArg, c);
        b.addTarget(Name.rightArg, c);


    }

    private Eq start(Eq f) {
        new Thread(f).start();
        return f;
    }

    private Func<Boolean> start(Gt f) {
        new Thread(f).start();
        return f;
    }

    private Func<Integer> start(Func<Integer> f) {
        new Thread(f).start();
        return f;
    }

    private If<Integer> start(If<Integer> f) {
        new Thread(f).start();
        return f;
    }

    private SIf<Integer> start(SIf<Integer> f) {
        new Thread(f).start();
        return f;
    }
    @Ignore
    @Test
    public void testIf() {

        /*
         * const mul = a * b;

         * output = if (mul > a + b)
         *              mul
         *          else
         *             a + b
         */


        Integer a = _1;
        Integer b = _4;

        If<Integer> _if = start(condInt(start(gt())));

        Func add = start(add())
                .addTarget(Name.rightArg, _if.cond)
                .addTarget(Name.onFalse, _if);

        Func mul = start(mul())
                .addTarget(Name.leftArg, _if.cond)
                .addTarget(Name.onTrue, _if);

        _if.addTarget(Name.result, printMsg);

        add.recieve(message(Name.leftArg, a));
        add.recieve(message(Name.rightArg, b));

        mul.recieve(message(Name.leftArg, a));
        mul.recieve(message(Name.rightArg, b));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testSif() {
        /*
        
          output = if (a > b)
                       a - b
                   else
                      b - a
         */

        Integer a = _4;
        Integer b = _2;

        SIf<Integer> _if = start(SIf.condInt(start(gt())));
        _if.addTarget(Name.result, printMsg);

        Func onTrue = start(sub()).addTarget(Name.onTrue, _if);
        Func onFalse = start(sub()).addTarget(Name.onFalse, _if);

        _if.propagateOnTrue(Name.a, Name.leftArg, onTrue);
        _if.propagateOnTrue(Name.b, Name.rightArg, onTrue);

        _if.propagateOnFalse(Name.b, Name.leftArg, onFalse);
        _if.propagateOnFalse(Name.a, Name.rightArg, onFalse);


        _if.recieve(message(Name.a, a));
        _if.recieve(message(Name.b, b));
        _if.cond.recieve(message(Name.leftArg, a));
        _if.cond.recieve(message(Name.rightArg, b));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Action expect(String key, Integer value) {
        return action(i -> {
            //assertEquals(Integer.valueOf(value), i.params.get(key));
            System.out.println(i.toString());
        });
    }

}