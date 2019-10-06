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
    public void matchGrowing() {
        assertTrue(p.matchGrowing(list(), list()));
        assertTrue(p.matchGrowing(list(), list(1)));
        assertTrue(p.matchGrowing(list(1), list(1)));
        assertTrue(p.matchGrowing(list(1), list(1, 2)));

        assertFalse(p.matchGrowing(list(1), list()));
        assertFalse(p.matchGrowing(list(1, 2), list(1)));
        assertFalse(p.matchGrowing(list(1, 2), list(2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void matchGrowingInvalidArgument1() {
        assertTrue(p.matchGrowing(list(-1), list()));
    }

    @Test
    public void matchShringking() {
        assertTrue(p.matchShrinking(list(-1), list(-1)));
        assertTrue(p.matchShrinking(list(1, -2), list(-1, -2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void matchShringking_inputCantBeEmpty() {
        assertTrue(p.matchShrinking(list(), list()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void matchShringking_lengthMustBeEquals() {
        assertTrue(p.matchShrinking(list(-1), list(1, -2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void matchShringkingInvalidArgument_rejectGrowing() {
        assertTrue(p.matchShrinking(list(1, 2), list(1, -2)));
    }

    private List<Long> list(Integer... iii) {
        return Arrays.stream(iii).map(Long::valueOf).collect(Collectors.toList());
    }
}