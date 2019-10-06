package nano.ingredients;

import org.jooq.lambda.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class ComputationPaths {
    // TODO Map<executionId, paths within that execution>
    private List<ComputationPath> paths = new ArrayList<>();

    public boolean add(ComputationPath p) {
        if (p.isEmpty()) {
            throw new IllegalArgumentException();
        }

        if (getMatches(p).isEmpty()) {
            paths.add(p);
            return true;
        }
        return false;
    }

    List<ComputationPath> getMatches(ComputationPath path) {
        List<ComputationPath> candidates = paths.stream()
                .filter(i -> i.executionId == path.executionId)
                .filter(i -> i.getSum() >= path.getSum())
                .collect(Collectors.toList());

        return path.getLastPopped() == null ? matchGrowing(path, candidates) : matchShrinking(path, candidates);
    }

    private List<ComputationPath> getGrowing(List<ComputationPath> candidates) {
        return candidates.stream()
                .filter(c -> !c.isEmpty() && c.items().get(c.size() - 1) > 0)
                .collect(Collectors.toList());
    }

    private List<ComputationPath> matchGrowing(ComputationPath p, List<ComputationPath> candidates) {
        return candidates.stream()
                .filter(i -> matchGrowing(p.items(), i.items()))
                .collect(Collectors.toList());
    }

    boolean matchGrowing(List<Long> newPath, List<Long> storedPaths) {
        if (newPath.size() > 0 && newPath.get(newPath.size() - 1) < 0) {
            throw new IllegalArgumentException();
        }

        if (storedPaths.size() < newPath.size()) {
            return false;
        }

        return seq(storedPaths)
                .zip(seq(newPath))
                .allMatch(i -> i.v1.equals(i.v2));
    }

    private List<ComputationPath> matchShrinking(ComputationPath newPath, List<ComputationPath> storedCandidates) {
        return storedCandidates.stream()
                .filter(i -> matchShrinking(newPath.items(), i.items()))
                .collect(Collectors.toList());
    }

    boolean matchShrinking(List<Long> newPath, List<Long> storedPath) {

        /*
         * size must be equal in any case
         *
         * replay 1,2,-3    replay 1,2,-3  replay 1,2,-3
         * stored 1,2,3     stored 1,2,-3  stored 1,-2,-3
         * match? N         match? Y       match? Y
         *
         * */

        if (storedPath.size() != newPath.size() || storedPath.isEmpty() || newPath.get(newPath.size() - 1) > 0) {
            throw new IllegalArgumentException();
        }

        return seq(storedPath).zip(seq(newPath))
                .allMatch(i -> {
                    Long r = i.v2;
                    Long s = i.v1;
                    return !(r < 0 && s > 0) && abs(r) == abs(s);
                });
    }

    private <T> Seq<T> seq(List<T> list) {
        T[] items = (T[]) list.toArray();
        return Seq.of(items);
    }

    public boolean isEmpty() {
        return paths.isEmpty();
    }

    public boolean contains(ComputationPath b) {
        return paths.contains(b);
    }
}
