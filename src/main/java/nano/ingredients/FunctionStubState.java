package nano.ingredients;

import java.util.ArrayList;
import java.util.List;

import static nano.ingredients.FunctionCall.functionCall;

class FunctionStubState extends State {
    private final FunctionStub stub;

    private FunctionCall<?> functionCall;
    private List<Message> pendingForPropagation = new ArrayList<>();

    FunctionStubState(FunctionStub stub, Origin origin) {
        super(origin);
        this.stub = stub;
    }

    private FunctionCall<?> createCall(Function<?> f) {
        FunctionCall<?> functionCall = functionCall(f);
        functionCall.label(f.address.label.toLowerCase());
        functionCall.returnTo(stub, Name.result);
        return functionCall;
    }

    void forward(Message m) {
        if (m.key.equals(stub.keyFunctionParameter)) {
            if (!(m.value instanceof Function))
            {
                throw new IllegalStateException();
            }
            functionCall = createCall((Function) m.value);
            pendingForPropagation.forEach(functionCall::tell);
            pendingForPropagation.clear();
            return;
        }

        if (functionCall != null) {
            functionCall.tell(m);
        } else {
            pendingForPropagation.add(m);
        }
    }


}
