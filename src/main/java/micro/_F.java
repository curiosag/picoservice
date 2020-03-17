package micro;

public interface _F {

    void addPropagation(String name, _F to);

    void addPropagation(String nameExpected, String namePropagated, _F to);

    _Ex createExecution(Env env, _Ex returnTo);
}
