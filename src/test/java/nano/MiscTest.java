package nano;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class MiscTest {

    @Test
    public void testZip(){
        List<Tuple2<String, Integer>> a = Seq
                .of("John", "Jane", "Dennis")
                .zip(Seq.of(24, 25, 27, 28))
                .toList();

        assertEquals(1,1);
    }
}
