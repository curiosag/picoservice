package micro;

public interface Actor {

    void receive(Value v);

    Address getAddress();

}
