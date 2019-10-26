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
        assertTrue(p.isMatching(computationNodes(1), computationNodes(1)));
        assertTrue(p.isMatching(computationNodes(1), computationNodes(1, 2)));

        assertFalse(p.isMatching(computationNodes(1, 2), computationNodes(1)));
        assertFalse(p.isMatching(computationNodes(1, 2), computationNodes(2)));

        assertTrue(p.isMatching(computationNodes(-1), computationNodes(-1)));
        assertTrue(p.isMatching(computationNodes(1), computationNodes(-1)));
        assertTrue(p.isMatching(computationNodes(1, -2), computationNodes(-1, -2)));
        assertTrue(p.isMatching(computationNodes(1, 2), computationNodes(1, -2)));
        assertFalse(p.isMatching(computationNodes(-1), computationNodes(1)));
        assertFalse(p.isMatching(computationNodes(-1, -2), computationNodes(1, -2)));
    }


    @Test(expected = IllegalArgumentException.class)
    public void matchShringking_inputCantBeEmpty1() {
        assertTrue(p.isMatching(computationNodes(), computationNodes(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void matchShringking_inputCantBeEmpty2() {
        assertTrue(p.isMatching(computationNodes(1), computationNodes()));
    }

    private List<ComputationNode> computationNodes(Integer... iii) {
        return Arrays.stream(iii).map(i -> new ComputationNode(String.valueOf(Math.abs(i)), i < 0)).collect(Collectors.toList());
    }
}