package micro;

public interface _F {

    void addPropagation(String name, _F to);

    void addPropagation(String nameExpected, String namePropagated, _F to);

    Ex createExecution(Env env, Ex returnTo);
}
