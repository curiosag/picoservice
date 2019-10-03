package nano.ingredients.tuples;

import nano.ingredients.ComputationBough;

import java.util.Optional;

public class ComputationBoughBranch extends Tuple<ComputationBough, Optional<ComputationBough>> {

    private ComputationBoughBranch(ComputationBough bough, Optional<ComputationBough> branchedOffFrom) {
        super(bough, branchedOffFrom);
    }

    public static ComputationBoughBranch of(ComputationBough bough, Optional<ComputationBough> branchedOffFrom){
        return new ComputationBoughBranch(bough, branchedOffFrom);
    }

    public ComputationBough getExecutionBough(){
        return left;
    }

    public Optional<ComputationBough> getBoughBranchedOffFrom(){
        return right;
    }
}
