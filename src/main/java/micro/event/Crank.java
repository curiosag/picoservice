package micro.event;

import micro.Id;

public interface Crank extends Id {

    boolean isMoreToDoRightNow();

    boolean isDone();

    void crank();

}
