package nano.ingredients;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComputationPaths {
    private Set<ComputationPath> paths = new HashSet<>();

    public void add(ComputationPath b){
        paths.add(b);
    }

    public List<ComputationPath> getMatches(ComputationPath b) {
        throw new IllegalStateException();
    }

    public boolean isEmpty(){
        return paths.isEmpty();
    }

    public boolean contains(ComputationPath b){
        return paths.contains(b);
    }
}
