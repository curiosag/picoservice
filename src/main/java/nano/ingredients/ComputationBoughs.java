package nano.ingredients;

import java.util.ArrayList;
import java.util.List;

public class ComputationBoughs {
    private ArrayList<ComputationBough> boughs = new ArrayList<>();

    public void add(ComputationBough b){
        boughs.add(b);
    }

    public List<ComputationBough> getMatches(ComputationBough b) {
        throw new IllegalStateException();
    }
}
