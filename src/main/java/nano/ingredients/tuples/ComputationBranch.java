package nano.ingredients.tuples;

import nano.ingredients.ComputationPath;

import java.util.Optional;

public class ComputationBranch extends Tuple<ComputationPath, Optional<ComputationPath>> {

    private ComputationBranch(ComputationPath bough, Optional<ComputationPath> branchedOffFrom) {
        super(bough, branchedOffFrom);
    }

    public static ComputationBranch of(ComputationPath bough, Optional<ComputationPath> branchedOffFrom){
        return new ComputationBranch(bough, branchedOffFrom);
    }

    public ComputationPath getExecutionPath(){
        return left;
    }

    public Optional<ComputationPath> getPathBranchedOffFrom(){
        return right;
    }
}
