package micro;

import micro.event.DependendExCreatedEvent;
import micro.event.ExEvent;

import java.util.List;

public interface Env extends Hydrator {

    Address getAddress();

    void note(ExEvent e);

    _Ex getTop();

    long getNextFId();

    long getNextExId();

    void addF(_F f);

    void setDelay(int delay);

    void stop();

    void start();

    void start(boolean recover);

    List<_Ex> createTargets(_Ex source, List<_F> targetTemplates);

    _Ex createExecution(F f);

    DependendExCreatedEvent createDependentExecutionEvent(_F f, _Ex returnTo, _Ex dependingOn);

    _Ex createExecution(_F f, _Ex returnTo);

    void relatchExecution(_F f, _Ex returnTo);

    int getMaxExCount();

    void schedule(_Ex ex);
}
