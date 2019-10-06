package nano.ingredients;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ComputationPathsTest {

    ComputationPaths p = new ComputationPaths();

    @Test
    public void isMatching() {
        assertTrue(p.isMatching(list(1), list(1)));
        assertTrue(p.isMatching(list(1), list(1, 2)));

        assertFalse(p.isMatching(list(1, 2), list(1)));
        assertFalse(p.isMatching(list(1, 2), list(2)));

        assertTrue(p.isMatching(list(-1), list(-1)));
        assertTrue(p.isMatching(list(1), list(-1)));
        assertTrue(p.isMatching(list(1, -2), list(-1, -2)));
        assertTrue(p.isMatching(list(1, 2), list(1, -2)));
        assertFalse(p.isMatching(list(-1), list(1)));
        assertFalse(p.isMatching(list(-1, -2), list(1, -2)));
    }


    @Test(expected = IllegalArgumentException.class)
    public void matchShringking_inputCantBeEmpty1() {
        assertTrue(p.isMatching(list(), list(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void matchShringking_inputCantBeEmpty2() {
        assertTrue(p.isMatching(list(1), list()));
    }

    private List<Long> list(Integer... iii) {
        return Arrays.stream(iii).map(Long::valueOf).collect(Collectors.toList());
    }
}