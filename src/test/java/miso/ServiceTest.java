package miso;

import miso.ingredients.Disperser;
import miso.ingredients.Func;
import org.junit.Test;

import static miso.Message.message;
import static miso.ingredients.Action.action;
import static miso.ingredients.Agg.agg;
import static miso.ingredients.Eq.eq;
import static miso.ingredients.FAgg.fagg;
import static miso.ingredients.GenericFunc.func;
import static miso.ingredients.Gt.gt;
import static miso.ingredients.If.branch;
import static miso.ingredients.Mul.mul;
import static miso.ingredients.Plus.plus;
import static org.junit.Assert.assertEquals;

public class ServiceTest {

    private static final Integer _8 = 8;
    private static final Integer _5 = 5;
    private static final Integer _4 = 4;
    private static final Integer _2 = 2;
    private static final Integer _1 = 1;

    @Test
    public void testFunc() {

        /*
            function add(v1, v2) = v1 + v2

            function max(v1, v2) = if (v1 > v2)
                                        v1
                                   else
                                        v2

         * main = if (i1 == i2)
         *              add(i1, i2)
         *          else
         *             max(i1, i2)
         */

        Disperser disp = new Disperser();

        Func add = func(fagg(plus().argKeys(Name.i1, Name.i2))).signOnTo(disp);
        Func max = fagg(branch().paramKeys(Name.decision, Name.i1, Name.i2)).signOnTo(disp);

        Func i1_gt_i2 = func(fagg(gt().argKeys(Name.i1, Name.i2))).resultKey(Name.decision).resultTo(max).signOnTo(disp);


        Func main = fagg(branch().paramKeys(Name.decision, Name.onTrue, Name.onFalse)).signOnTo(disp);
        Func i1_eq_i2 = fagg(eq().argKeys(Name.i1, Name.i2)).resultKey(Name.decision).resultTo(main).signOnTo(disp);
        add.resultKey(Name.onTrue).resultTo(main);
        max.resultKey(Name.onFalse).resultTo(main);

        main.resultTo(expect(Name.result, _8));
        disp.disperse(message()
                .put(Name.i1, _4)
                .put(Name.i2, _4));

        main.resultTo(expect(Name.result, _5));
        disp.disperse(message()
                .put(Name.i1, _5)
                .put(Name.i2, _4));

    }

    @Test
    public void testIf() {

        /*
         * const mul = i1 * i2;
         * output = if (mul > i1 + i2)
         *              mul
         *          else
         *             i1 + i2
         */


        Actress branch = branch().paramKeys(Name.decision, Name.product, Name.sum);
        Actress iff = agg(branch().paramKeys(Name.decision, Name.product, Name.sum));

        Actress ageGtHeight = agg(gt().argKeys(Name.product, Name.sum).resultKey(Name.decision).resultTo(iff));

        Actress add = agg(plus().argKeys(Name.i1, Name.i2).resultKey(Name.sum).resultTo(ageGtHeight, iff));
        Actress mul = agg(mul().argKeys(Name.i1, Name.i2).resultKey(Name.product).resultTo(ageGtHeight, iff));

        branch.resultTo(expect(Name.result, _5));
        add.recieve(new Message()
                .put(Name.i1, _4)
                .put(Name.i2, _1));

        mul.recieve(Message.of(Name.i2, _1));
        mul.recieve(Message.of(Name.i1, _4));

        Message msg = new Message()
                .put(Name.i1, _4)
                .put(Name.i2, _2);

        branch.resultTo(expect(Name.result, _8));
        add.recieve(msg);
        mul.recieve(msg);

    }


    private Actress printMsg() {
        return action(i -> System.out.println(i.toString()));
    }

    private Actress expect(String key, Integer value) {
        return action(i -> {
            assertEquals(Integer.valueOf(value), i.params.get(key));
            System.out.println(i.toString());
        });
    }

}