package micro;

import micro.event.Crank;
import micro.event.ExEvent;

public interface _Ex extends Id, Crank, Actor, _FlowDirections {

    _Ex returnTo();

    _F getTemplate();

    void recover(ExEvent e);

    String getLabel();

}
