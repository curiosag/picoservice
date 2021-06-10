package micro.event;

import micro.Id;

public interface Crank extends Id {

    boolean isDone();

    void crank();

}
