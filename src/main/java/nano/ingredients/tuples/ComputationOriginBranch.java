package nano.ingredients.tuples;

import nano.ingredients.ComputationPath;
import nano.ingredients.Origin;

import java.util.Optional;

public class ComputationOriginBranch extends Tuple<Origin, Optional<ComputationPath>> {

    private ComputationOriginBranch(Origin bough, Optional<ComputationPath> branchedOffFrom) {
        super(bough, branchedOffFrom);
    }

    public static ComputationOriginBranch of(Origin bough, Optional<ComputationPath> branchedOffFrom){
        return new ComputationOriginBranch(bough, branchedOffFrom);
    }

    public Origin getOrigin(){
        return left;
    }

    public Optional<ComputationPath> getBoughBranchedOffFrom(){
        return right;
    }
}
