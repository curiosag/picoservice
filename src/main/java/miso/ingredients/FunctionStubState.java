package miso.ingredients;

import java.util.ArrayList;
import java.util.List;

import static miso.ingredients.FunctionCall.functionCall;

class FunctionStubState extends State {
    private final FunctionStub stub;

    private FunctionCall<?> functionCall;
    private List<Message> pendingForPropagation = new ArrayList<>();

    FunctionStubState(FunctionStub stub, Origin origin) {
        super(origin);
        this.stub = stub;
    }

    private FunctionCall<?> createCall(FunctionSignature<?> f) {
        FunctionCall<?> functionCall = functionCall(f);
        functionCall.label(f.address.label.toLowerCase());
        functionCall.returnTo(stub, Name.result);
        return functionCall;
    }

    void forward(Message m) {
        if (m.key.equals(stub.keyFunctionParameter)) {
            if (!(m.value instanceof FunctionSignature))
            {
                throw new IllegalStateException();
            }
            functionCall = createCall((FunctionSignature) m.value);
            pendingForPropagation.forEach(functionCall::receive);
            pendingForPropagation.clear();
            return;
        }

        if (functionCall != null) {
            functionCall.receive(m);
        } else {
            pendingForPropagation.add(m);
        }
    }
}
