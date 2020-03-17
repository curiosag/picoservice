package nano.ingredients;

import nano.ingredients.tuples.ComputationOriginBranch;

import java.io.Serializable;

import static nano.ingredients.Ensemble.attachActor;
import static nano.ingredients.Message.message;

public class FunctionCall<T extends Serializable> extends Function<T> {

    public final Function<T> function;

    protected FunctionCall(Function<T> f) {
        this.function = f;
    }

    protected FunctionCall(Function<T> f, Address address) {
        super(address);
        this.function = f;
    }

    public static <T extends Serializable> FunctionCall<T> functionCall(Function<T> body, Address address) {
        FunctionCall<T> result = new FunctionCall<>(body, address);
        attachActor(result);
        return result;
    }

    public static <T extends Serializable> FunctionCall<T> functionCall(Function<T> body) {
        FunctionCall<T> result = new FunctionCall<>(body);
        attachActor(result);
        return result;
    }

    @Override
    protected boolean shouldPropagate(String key) {
        return false;
    }

    @Override
    protected FunctionState newState(Origin origin) {
        return new FunctionState(origin);
    }

    @Override
    protected void processInner(Message m, FunctionState state) {
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
                hdlForwardings(origin, onReturn);
            }

            origin = origin.popCall();
            if (!this.address.id.equals(origin.getComputationPath().getLastPopped().id)) {
                String fmt = "Function call %s attempted to pop itself, but found on stack %s";
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
                origin = maybeOriginBranchedOffFrom.getOrigin();
                getState(origin);
            }
            function.tell(message.origin(origin));
        }
    }

    private boolean isConst(Message message) {
        return message.origin.getSender().equals(this);
    }


    @Override
    public void receiveRecover(Message m) {
        trace(m);
        receive(m);
    }


    @Override
    public boolean shouldPersist(Message m) {
        return true;
    }

}