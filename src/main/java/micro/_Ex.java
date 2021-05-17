package micro;

import micro.event.Crank;
import micro.event.ExEvent;

public interface _Ex extends Id, Crank {

    _Ex returnTo();

    _F getTemplate();

    void receive(Value v);

    void recover(ExEvent e);

    Address getAddress();

    String getLabel();

}
