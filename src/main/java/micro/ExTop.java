package micro;

import micro.event.ExEvent;

public class ExTop implements  _Ex, Id {


    public static final ExTop instance = new ExTop(Address.localhost);

    public final static long TOP_ID = 0;

    private final _F template;
    private final Address address;

    public ExTop(Address address) {
        this.address = address;
        this.template = new FTop();
    }

    @Override
    public String toString() {
        return "TOP";
    }

    @Override
    public _Ex returnTo() {
        return this;
    }

    @Override
    public _F getTemplate() {
        return template;
    }

    @Override
    public void receive(Value v) {

    }

    @Override
    public void recover(ExEvent e) {

    }

    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public String getLabel() {
        return "TOP";
    }

    @Override
    public long getId() {
        return TOP_ID;
    }

    @Override
    public void setId(long value) {

    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public void crank() {

    }
}
