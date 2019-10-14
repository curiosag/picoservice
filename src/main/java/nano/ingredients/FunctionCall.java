package nano.ingredients;

import nano.ingredients.tuples.ComputationOriginBranch;

import java.io.Serializable;
import java.util.List;

import static nano.ingredients.Ensemble.attachActor;
import static nano.ingredients.Message.message;

public class FunctionCall<T extends Serializable> extends Function<T> {

    private final Function<T> function;

    protected FunctionCall(Function<T> f) {
        this.function = f;
    }

    public static <T extends Serializable> FunctionCall<T> functionCall(Function<T> body) {
        FunctionCall<T> result = new FunctionCall<>(body);
        attachActor(result);
        return result;
    }

    @Override
    protected boolean belongsToMe(String key) {
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

            if (!this.address.id.equals(origin.getComputationPath().getLastPopped())) {
                String fmt = "Function call %s attempted to pop itself, but found on stack %d";
                throw new IllegalStateException(String.format(fmt, this.address.toString(), origin.getComputationPath().getLastPopped()));
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
            getTracer().tell(branchMessage(maybeOriginBranchedOffFrom.getBoughBranchedOffFrom().get(), origin));
        }
    }

    private Message branchMessage(ComputationPath b, Origin origin) {
        return new Message(Name.computationBranch, b, origin);
    }


    private boolean isConst(Message message) {
        return message.origin.getSender().equals(this);
    }


    @Override
    public void receiveRecover(Message m) {
        trace(m);
        receive(m.setReplay(isReplay(m)));
    }

    private boolean isReplay(Message m) {
        ComputationPaths ps = getTracer().getReplayedComputationPaths();
        ComputationPath p = m.origin.getComputationPath();

        if (ps.isEmpty()) {
            return false;
        }
        ComputationPath expectedAfterFunctionCall;
        if (!m.key.equals(Name.result)) {
            expectedAfterFunctionCall = p.pop();
        } else { // we're piling up stack frames
            expectedAfterFunctionCall = p.push(this.address.id).getExecutionPath();
        }
        List<ComputationPath> matches = ps.getMatching(expectedAfterFunctionCall);
        return !matches.isEmpty();
    }

    @Override
    public boolean shouldPersist(Message m) {
        return true;
    }

}
