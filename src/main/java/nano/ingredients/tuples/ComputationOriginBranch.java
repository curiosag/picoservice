package nano.ingredients.tuples;

import nano.ingredients.ComputationBough;
import nano.ingredients.Origin;

import java.util.Optional;

public class ComputationOriginBranch extends Tuple<Origin, Optional<ComputationBough>> {

    private ComputationOriginBranch(Origin bough, Optional<ComputationBough> branchedOffFrom) {
        super(bough, branchedOffFrom);
    }

    public static ComputationOriginBranch of(Origin bough, Optional<ComputationBough> branchedOffFrom){
        return new ComputationOriginBranch(bough, branchedOffFrom);
    }

    public Origin getOrigin(){
        return left;
    }

    public Optional<ComputationBough> getBoughBranchedOffFrom(){
        return right;
    }
}
