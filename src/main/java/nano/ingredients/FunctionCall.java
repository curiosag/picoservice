package nano.ingredients;

import nano.ingredients.tuples.ComputationOriginBranch;

import java.io.Serializable;

import static nano.ingredients.Ensemble.wire;
import static nano.ingredients.Message.message;

public class FunctionCall<T extends Serializable> extends Function<T> {

    private final Function<T> function;

    protected FunctionCall(Function<T> f) {
        this.function = f;
    }

    public static <T extends Serializable> FunctionCall<T> functionCall(Function<T> body) {
        FunctionCall<T> result = new FunctionCall<>(body);
        wire(result);
        return result;
    }

    @Override
    protected boolean isParameter(String key) {
        return true;
    }

    @Override
    protected State newState(Origin origin) {
        return new State(origin);
    }

    @Override
    protected void processInner(Message m, State state) {
        throw new IllegalStateException();
    }

    @Override
    public void process(Message message) {
        trace(message);

        Origin origin = message.origin.sender(this);
        if (message.hasAnyKey(Name.result, Name.error)) {
            removeState(origin);
            // returnResult((T) message.value, origin); doesn't work here, the popping messes it up
            // it must be hdlOnReturn, popCall, returnTo.tell
            if (message.hasKey(Name.result)) {
                hdlForwarings(origin, onReturn);
            }

            origin = origin.popCall();

            if (!this.address.id.equals(origin.getComputationBough().getLastPopped())) {
                String fmt = "Function call %s attempted to pop itself, but found on stack %d";
                throw new IllegalStateException(String.format(fmt, this.address.toString(), origin.getComputationBough().getLastPopped()));
            }

            if (message.hasKey(Name.result)) {
                returnTo.tell(message(returnKey, (T) message.getValue(), origin));
            } else {
                returnTo.tell(message.origin(origin));
            }

        } else {
            if (!isConst(message)) // const already comes with proper stack
            {
                ComputationOriginBranch maybeOriginBranchedOffFrom = origin.pushCall(this);
                notifyBranching(maybeOriginBranchedOffFrom, origin);
                origin = maybeOriginBranchedOffFrom.getOrigin();
                getState(origin);
            }
            function.tell(message.origin(origin));
        }

    }

    private void notifyBranching(ComputationOriginBranch maybeOriginBranchedOffFrom, Origin origin) {
        if (maybeOriginBranchedOffFrom.getBoughBranchedOffFrom().isPresent()) {
            tracer.tell(branchMessage(maybeOriginBranchedOffFrom.getBoughBranchedOffFrom().get(), origin));
        }
    }

    private Message branchMessage(ComputationBough b, Origin origin) {
        return new Message(Name.computationBranch, b, origin);
    }


    private boolean isConst(Message message) {
        return message.origin.getSender().equals(this);
    }

}
