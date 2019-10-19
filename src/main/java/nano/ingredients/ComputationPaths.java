package nano.ingredients;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                .filter(stored -> isMatching(pathNew.items(), stored.items()))
                .collect(Collectors.toList());

    }

    public boolean isMatching(List<ComputationNode> pathNew, List<ComputationNode> pathStored) {
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
            ComputationNode n = pathNew.get(i);
            ComputationNode s = pathStored.get(i);
            if (!n.id.equals(s.id) || n.callReturned && ! s.callReturned) {
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
