package nano.ingredients.tuples;

import nano.ingredients.ComputationStack;

public class Replay extends Tuple<String, ComputationStack> {

    public Replay(String senderId, ComputationStack stack) {
        super(senderId, stack);
    }

}
