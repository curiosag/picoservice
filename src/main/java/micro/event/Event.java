package micro.event;

import micro.Hydratable;
import micro.event.serialization.Incoming;
import micro.event.serialization.Outgoing;
import micro.event.serialization.Serioulizable;

public abstract class Event implements Hydratable, Serioulizable {

    Event()
    {
    }

    @Override
    public void write(Outgoing out) {

    }

    @Override
    public void read(Incoming in) {

    }
}
