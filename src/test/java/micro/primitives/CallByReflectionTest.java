package micro.primitives;

import micro.Address;
import micro.Value;
import micro._Ex;
import micro._F;
import micro.event.ExEvent;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class CallByReflectionTest {

    public static String m(Integer a, Short b) {
        return "m1";
    }

    public static String m(Short a, Integer b) {
        return "m2";
    }

    private CallByReflection call = new CallByReflection("micro.primitives.CallByReflectionTest", "m", List.of("a", "b"));

    @Test
    public void testExecute() {

        _Ex ex = getEx();
        short s = 0;
        int i = 1;

        HashMap<String, Value> m1params = new HashMap<>() {{
            put("a", Value.of("a", i, ex));
            put("b", Value.of("b", s, ex));
        }};

        assertEquals("m1", call.execute(m1params));

        HashMap<String, Value> m2params = new HashMap<>() {{
            put("a", Value.of("a", s, ex));
            put("b", Value.of("b", i, ex));
        }};

        assertEquals("m2", call.execute(m2params));
    }

    @Test(expected = IllegalStateException.class)
    public void testFailExecute() {

        _Ex ex = getEx();
        short s = 0;

        HashMap<String, Value> params = new HashMap<>() {{
            put("a", Value.of("a", s, ex));
            put("b", Value.of("b", s, ex));
        }};

        call.execute(params);
    }

    private _Ex getEx(){
        return new _Ex() {
            @Override
            public _Ex returnTo() {
                return null;
            }

            @Override
            public _F getTemplate() {
                return null;
            }

            @Override
            public void recover(ExEvent e) {

            }

            @Override
            public String getLabel() {
                return null;
            }

            @Override
            public void receive(Value v) {

            }

            @Override
            public Address getAddress() {
                return null;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public void crank() {

            }

            @Override
            public long getId() {
                return 0;
            }

            @Override
            public void setId(long value) {

            }
        };
    }
}