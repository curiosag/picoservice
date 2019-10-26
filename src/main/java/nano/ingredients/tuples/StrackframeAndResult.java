package nano.ingredients.tuples;

import nano.ingredients.Message;

public class StrackframeAndResult extends Tuple<Message, Message> {

    public StrackframeAndResult(Message stackFrame, Message result) {
        super(stackFrame, result);
    }

    public Message getStackFrame(){
        return left;
    }

    public Message getResult(){
        return right;
    }
}
