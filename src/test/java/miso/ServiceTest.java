package miso;

import miso.ingredients.*;
import miso.message.Message;
import miso.message.Name;
import org.junit.Ignore;
import org.junit.Test;

import static miso.ingredients.Action.action;
import static miso.ingredients.Add.add;
import static miso.ingredients.Eq.eq;
import static miso.ingredients.Gt.gt;
import static miso.ingredients.If.condInt;
import static miso.ingredients.Mul.mul;
import static miso.ingredients.Sub.sub;
import static miso.message.Name.a;

public class ServiceTest {

    private static Long zero = 0L;

    private static final Integer _8 = 8;
    private static final Integer _5 = 5;
    private static final Integer _4 = 4;
    private static final Integer _2 = 2;
    private static final Integer _3 = 3;
    private static final Integer _1 = 1;
    private static final Integer _0 = 0;

    private static Action printMsg = action(i -> System.out.println(i.toString()));
    static{
        new Thread(printMsg).start();
    }

    private static Address main = new Address("main");

    private Message message(String key, Object value) {
        return Message.of(key, value, new Source(printMsg, 0L, 0));
    }


    @Ignore
    @Test
    public void testPing() {

        /*

            print(1 + 2)

         */

        Func<Integer> add = start(add());
        Action printMsg = action(i -> System.out.println(i.toString()));

        add.returnTo("pingedResult", printMsg);

        printMsg.addPing(add);

        printMsg.recieve(new Message(Name.ping, null, new Source(printMsg, 0L,0)));


    }

    @Ignore
    @Test
    public void testFunctionCall() {

        /*

            function add(a, b) = (a + b);

            print(add(1, 2))

         */

        Func<Integer> add = start(add());
        Func<Integer> fAdd= start(new Function<>(add));

        Func<Integer> callAddA = start(new FunctionCall<>(fAdd));
        Func<Integer> callAddB = start(new FunctionCall<>(fAdd));

        callAddA.returnTo("resultCallA", printMsg);
        callAddB.returnTo("resultCallB", printMsg);

        Source printMsgSource = new Source(printMsg, 0L, 0);
        callAddA.recieve(new Message(Name.a, 1, printMsgSource));
        callAddA.recieve(new Message(Name.b, 2, printMsgSource));

        waitSome();
    }

    @Ignore
    @Test
    public void testRecursion() {

        /*

            function fac(a) = if (a = 0 )
                                        1
                                   else
                                        a * fac(a - 1);


            echo(fac(3));

         */

        Func<Boolean> eq = start(eq());

        SIf<Integer> _if = start(SIf.condInt(eq));
        Func<Integer> funcFac = start(new Function<>(_if));

        Func<Integer> sub = start(sub());
        Func<Integer> mul = start(mul());

        Func<Integer> callFacRecursively = start(new FunctionCall<>(funcFac));

        callFacRecursively.returnTo(Name.rightArg, mul);
        _if.returnTo(Name.result, funcFac);
        eq.returnTo(Name.decision, _if);
        mul.returnTo(Name.onFalse, _if);

        funcFac.addPropagation(a, _if.cond);

        // if (a = 0)

        _if.addPropagation(Name.a, Name.leftArg, eq);
        eq.addConst(Name.rightArg, 0);
        // 1
        _if.addConst(Name.onTrue, 1);
        // else
        //     a * fac(a - 1)
        _if.addPropagation(Name.a, Name.leftArg, mul);
        mul.addPropagation(Name.a, Name.a, callFacRecursively);
        callFacRecursively.addPropagation(Name.a, Name.rightArg, sub);
        sub.addConst(Name.rightArg, _1);

        // echo(fac(3))
        Func<?> callFac = start(new FunctionCall<>(funcFac));
        callFac.returnTo("planFuncCallResult", printMsg);
        Message m = new Message(Name.a, _3, new Source(printMsg, 0L, 0));
        funcFac.recieve(m);

        waitSome();

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
    public void testSIf() {

        /*
         * const mul = a * b;

         * output = if (mul > a + b)
         *              mul
         *          else
         *             a + b
         */

        Integer a = _1;
        Integer b = _4;

        Func<Boolean> gt = start(gt());
        If<Integer> _if = start(condInt(gt));

        Func add = start(add())
                .returnTo(Name.rightArg, gt)
                .returnTo(Name.onFalse, _if);

        Func mul = start(mul())
                .returnTo(Name.leftArg, gt)
                .returnTo(Name.onTrue, _if);

        _if.returnTo(Name.result, printMsg);

        add.recieve(message(Name.leftArg, a));
        add.recieve(message(Name.rightArg, b));

        mul.recieve(message(Name.leftArg, a));
        mul.recieve(message(Name.rightArg, b));

        waitSome();
    }

    private void waitSome() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testif() {
        /*
        
          output = if (a > b)
                       a - b
                   else
                      b - a

          print(output)

         */

        Func<Boolean> gt = start(gt());

        If<Integer> _if = start(If.condInt(gt));

        Func<Integer> subTrueBranch = start(sub());
        Func<Integer> subFalseBranch = start(sub());

        gt.returnTo(Name.decision, _if);
        subTrueBranch.returnTo(Name.onTrue, _if);
        subFalseBranch.returnTo(Name.onFalse, _if);
        _if.returnTo(Name.result, printMsg);

        _if.addPropagation(Name.a, gt);
        _if.addPropagation(Name.b, gt);
        _if.addPropagation(Name.a, subTrueBranch);
        _if.addPropagation(Name.b, subTrueBranch);
        _if.addPropagation(Name.a, subFalseBranch);
        _if.addPropagation(Name.b, subFalseBranch);

        _if.recieve(new Message(Name.a, 4, new Source(printMsg, 0L, 0)));

        waitSome();
    }

    private Action expect(String key, Integer value) {
        return action(i -> {
            //assertEquals(Integer.valueOf(value), i.params.get(key));
            System.out.println(i.toString());
        });
    }

}