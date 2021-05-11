package micro.event;

import micro.Ex;

public class InitialExecutionCreatedEvent extends ExecutionCreatedEvent{
    public InitialExecutionCreatedEvent(Ex ex) {
        super(ex);
    }

    //todo serialize
}
