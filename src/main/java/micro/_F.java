package micro;

public interface _F extends Id {

    void addPropagation(PropagationType type, String nameExpected, String namePropagated, _F to);

    _Ex createExecution(Env env, _Ex returnTo);
}
