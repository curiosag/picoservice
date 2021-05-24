package micro;

import micro.event.DependendExCreatedEvent;
import micro.event.ExEvent;

import java.util.List;

public interface Env extends Hydrator {

    void note(ExEvent e);

    Address getAddress();

    _Ex getTop();

    long getNextFId();

    long getNextExId();

    void addF(_F f);

    void setDelay(int delay);

    void stop();

    void start();

    void start(boolean recover);

    List<_Ex> allocatePropagationTargets(_Ex source, List<_F> targetTemplates);

    _Ex createExecution(F f);

    DependendExCreatedEvent createDependentExecutionEvent(_F f, _Ex returnTo, _Ex dependingOn);

    _Ex createExecution(_F f, _Ex returnTo);

    void relatchExecution(long exId, _F f, _Ex returnTo);

}
