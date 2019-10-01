package nano.ingredients;

import java.util.ArrayList;
import java.util.List;

class FunctionStubState extends State {
    FunctionCall<?> functionCall;
    List<Message> pendingForPropagation = new ArrayList<>();

    FunctionStubState(Origin origin) {
        super(origin);
    }
}
