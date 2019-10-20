package nano.ingredients.tuples;

import nano.ingredients.ComputationStack;
import nano.ingredients.Message;

import java.util.List;
import java.util.Map;

public class ReplayData extends Tuple<Map<ComputationStack, Map<String, Message>>, Map<ComputationStack, List<Message>>> {

    public ReplayData(Map<ComputationStack, Map<String, Message>> recovered, Map<ComputationStack, List<Message>> toReplay) {
        super(recovered, toReplay);
    }

    public Map<ComputationStack, Map<String, Message>> getRecovered(){
        return left;
    }

    public Map<ComputationStack, List<Message>> toReplay(){
        return right;
    }

}
