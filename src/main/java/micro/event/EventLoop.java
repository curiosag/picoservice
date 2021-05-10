package micro.event;

import micro._Ex;

public interface EventLoop {

    void register(_Ex e);

    void loop();
}
