package nano.ingredients.tuples;

public class ComputationTreeLeaf extends Tuple<Long, Long> {

    private ComputationTreeLeaf(Long executionId, Long FunctionCallId) {
        super(executionId, FunctionCallId);
    }

    public static ComputationTreeLeaf of(Long left, Long right){
        return new ComputationTreeLeaf(left,right);
    }

    public Long getExecutionId(){
        return left;
    }

    public Long getFunctionCallId(){
        return right;
    }
}
