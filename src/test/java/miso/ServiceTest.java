package miso;

import org.junit.Test;

import static miso.ingredients.Action.action;
import static miso.ingredients.Agg.agg;
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
    private static final Integer _3 = 3;
    private static final Integer _2 = 2;
    private static final Integer _1 = 1;

    @Test
    public void testFunc() {

        /*
            function add(v1, v2) = v1 + v2

         * output = if (i1 * i2 > i1 + i2)
         *              mul
         *          else
         *             add(i1, i2)
         */


        Actress add = agg(func(plus()).resultKey(Name.sum).paramsRequired(Name.leftArg, Name.rightArg).resultTo(expect(Name.sum, _3)));
        add.recieve(Message.of(Name.leftArg, _1));
        add.recieve(Message.of(Name.rightArg, _2));

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



    private Actress expect(String key, Integer value) {
        return action(i -> {
            assertEquals(Integer.valueOf(value), i.params.get(key));
            System.out.println(i.toString());
        });
    }

}