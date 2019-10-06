package nano.ingredients;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComputationBoughs {
    private Set<ComputationBough> boughs = new HashSet<>();

    public void add(ComputationBough b){
        boughs.add(b);
    }

    public List<ComputationBough> getMatches(ComputationBough b) {
        throw new IllegalStateException();
    }

    public boolean isEmpty(){
        return boughs.isEmpty();
    }

    public boolean contains(ComputationBough b){
        return boughs.contains(b);
    }
}
