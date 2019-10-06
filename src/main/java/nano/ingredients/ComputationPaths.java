package nano.ingredients;

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

        if (getMatching(p).isEmpty()) {
            paths.add(p);
            return true;
        }
        return false;
    }

    List<ComputationPath> getMatching(ComputationPath pathNew) {
        return paths.stream()
                .filter(stored -> stored.executionId == pathNew.executionId)
                .filter(stored -> stored.getSum() >= pathNew.getSum())
                .filter(stored -> isMatching(pathNew.items(), stored.items()))
                .collect(Collectors.toList());

    }

    boolean isMatching(List<Long> pathNew, List<Long> pathStored) {
        /*
         * precondition: ! new.isEmpty()
         *
         * new    1,2       new    1       new    1
         * stored 1         stored 1       stored 1,2
         * match? N         match? Y       match? Y
         *       
         * new    1,2,-3    new    1,2,-3  new    1,2,-3
         * stored 1,2,3     stored 1,2,-3  stored 1,-2,-3
         * match? N         match? Y       match? Y
         *
         * */

        if(pathNew.isEmpty() || pathStored.isEmpty())
        {
            throw new IllegalArgumentException();
        }

        if (pathNew.size() > pathStored.size()) {
            return false;
        }
        
        for (int i = pathNew.size() - 1; i >= 0; i--) {
            Long n = pathNew.get(i);
            Long s = pathStored.get(i);
            if ((n < 0 && s > 0) || abs(n) != abs(s)) {
                return false;
            }
        }

        return true;
    }

    public boolean isEmpty() {
        return paths.isEmpty();
    }

    public boolean contains(ComputationPath b) {
        return paths.contains(b);
    }
}
