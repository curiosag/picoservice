package miso;

import org.junit.Test;

import static miso.ingredients.Action.action;
import static miso.ingredients.Gt.gt;
import static miso.ingredients.If.branch;
import static miso.ingredients.Mul.mul;
import static miso.ingredients.Plus.plus;
import static org.junit.Assert.assertEquals;

public class ServiceTest {


    @Test
    public void testFunc() {

        Integer _4 = 4;
        Integer _2 = 1;

        /*
            function add(v1, v2) = v1 + v2

         * output = if (i1 * i2 > i1 + i2)
         *              mul
         *          else
         *             add(i1, i2)
         */


    }

    @Test
    public void testIf() {
        Integer _8 = 8;
        Integer _5 = 5;
        Integer _4 = 4;
        Integer _2 = 2;
        Integer _1 = 1;
        /*
         * const mul = i1 * i2;
         * output = if (mul > i1 + i2)
         *              mul
         *          else
         *             i1 + i2
         */


        Actress iff = branch().paramKeys(Name.decision, Name.product, Name.sum);

        Actress ageGtHeight = gt().argKeys(Name.product, Name.sum).resultKey(Name.decision).resultTo(iff);

        Actress add = plus().argKeys(Name.i1, Name.i2).resultKey(Name.sum).resultTo(ageGtHeight, iff);
        Actress mul = mul().argKeys(Name.i1, Name.i2).resultKey(Name.product).resultTo(ageGtHeight, iff);

        iff.resultTo(expect(Name.result, _5));
        add.recieve(new Message()
                .put(Name.i1, _4)
                .put(Name.i2, _1));

        mul.recieve(Message.of(Name.i2, _1));
        mul.recieve(Message.of(Name.i1, _4));

        Message msg = new Message()
                .put(Name.i1, _4)
                .put(Name.i2, _2);

        iff.resultTo(expect(Name.result, _8));
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